package com.personal.lld.service;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.BookingStatus;
import com.personal.lld.domain.CancellationPolicy;
import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.Hotel;
import com.personal.lld.domain.NightlyPrice;
import com.personal.lld.domain.RefundDecision;
import com.personal.lld.domain.RoomType;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.User;
import com.personal.lld.repository.BookingRepository;
import com.personal.lld.repository.CancellationPolicyRepository;
import com.personal.lld.repository.HotelRepository;
import com.personal.lld.repository.RoomTypeRepository;
import com.personal.lld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final long HOLD_TTL_MS = 15 * 60 * 1000L;
    private static final long PRICE_TOLERANCE_MINOR = 100L;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final PolicyService policyService;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final TransactionService transactionService;

    public Booking createBooking(
            String userId,
            String hotelId,
            String roomTypeId,
            DateRange range,
            long expectedTotalPriceMinor) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Room type not found: " + roomTypeId));

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found: " + hotelId));

        if (!hotel.isActive() || !roomType.isActive()) {
            throw new IllegalStateException("Hotel or room type is inactive");
        }

        if (!hotelId.equals(roomType.getHotelId())) {
            throw new IllegalArgumentException("Room type does not belong to hotel");
        }

        validateDateRange(range);

        if (!inventoryService.checkAvailability(hotelId, roomTypeId, range, 1)) {
            throw new IllegalStateException("No availability for the requested dates");
        }

        List<NightlyPrice> nightlyPrices = pricingService.rateStay(hotelId, roomTypeId, range);
        if (nightlyPrices.isEmpty()) {
            throw new IllegalStateException("Could not fetch prices");
        }

        long calculatedTotal = pricingService.computeTotal(nightlyPrices);

        if (expectedTotalPriceMinor > 0
                && Math.abs(calculatedTotal - expectedTotalPriceMinor) > PRICE_TOLERANCE_MINOR) {
            throw new IllegalStateException(
                    "Price mismatch: expected " + expectedTotalPriceMinor + ", got " + calculatedTotal);
        }

        long now = System.currentTimeMillis();

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                user.getId(),
                hotel.getId(),
                roomType.getId(),
                range.getCheckInDateUtc(),
                range.getCheckOutDateUtc(),
                nightlyPrices,
                calculatedTotal,
                BookingStatus.CREATED,
                TransactionStatus.PENDING,
                null,
                0,
                0,
                0,
                now);

        log.info("Created booking {} for user {} at hotel {}", booking.getId(), userId, hotelId);
        return bookingRepository.save(booking);
    }

    public void cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this booking");
        }

        if (!BookingStateHandler.canCancel(booking)) {
            throw new IllegalStateException(
                    "Cannot cancel booking in current state: " + booking.getBookingStatus());
        }

        Hotel hotel = hotelRepository.findById(booking.getHotelId())
                .orElseThrow(() -> new IllegalStateException("Hotel not found"));

        CancellationPolicy policy = cancellationPolicyRepository
                .findById(hotel.getCancellationPolicyId())
                .orElseThrow(() -> new IllegalStateException("Cancellation policy not found"));

        RefundDecision decision = policyService.evaluateCancellation(
                booking, policy, System.currentTimeMillis());

        BookingStateHandler.transition(booking, BookingStatus.CANCELLED);

        if (decision.getRefundAmountMinor() > 0) {
            booking.setPaymentStatus(TransactionStatus.REFUNDED);
            transactionService.issueRefund(bookingId, decision.getRefundAmountMinor());
        } else if (booking.getPaymentStatus() == TransactionStatus.PENDING) {
            booking.setPaymentStatus(TransactionStatus.FAILED);
        }

        bookingRepository.save(booking);
    }

    public Booking checkIn(String bookingId, String roomId, long checkInTimeUtc) {
        Booking booking = getBooking(bookingId);
        BookingStateHandler.requireStatus(booking, BookingStatus.CONFIRMED);

        booking.setAllocatedRoomId(roomId);
        booking.setCheckInTimeUtc(checkInTimeUtc);
        BookingStateHandler.transition(booking, BookingStatus.CHECKED_IN);

        return bookingRepository.save(booking);
    }

    public Booking checkOut(String bookingId, long checkOutTimeUtc) {
        Booking booking = getBooking(bookingId);
        BookingStateHandler.requireStatus(booking, BookingStatus.CHECKED_IN);

        booking.setCheckOutTimeUtc(checkOutTimeUtc);
        BookingStateHandler.transition(booking, BookingStatus.CHECKED_OUT);

        return bookingRepository.save(booking);
    }

    public void processExpiredHolds() {
        long now = System.currentTimeMillis();

        for (Booking booking : bookingRepository.findExpiredHeldBookings(now)) {
            try {
                BookingStateHandler.transition(booking, BookingStatus.CANCELLED);
                booking.setPaymentStatus(TransactionStatus.FAILED);
                bookingRepository.save(booking);
                log.info("Expired booking hold released: {}", booking.getId());
            } catch (IllegalStateException ex) {
                log.warn("Could not release expired hold {}: {}", booking.getId(), ex.getMessage());
            }
        }
    }

    private Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    private void validateDateRange(DateRange range) {
        if (range == null || range.getCheckInDateUtc() >= range.getCheckOutDateUtc()) {
            throw new IllegalArgumentException(
                    "Invalid date range: check-in must be before check-out");
        }
    }
}
