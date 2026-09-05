package com.personal.lld.repository;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.BookingStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookingRepository {
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public List<Booking> findByUser(String userId) {
        return bookings.values().stream()
                .filter(booking -> userId.equals(booking.getUserId()))
                .toList();
    }

    public int countBookings(
            String hotelId,
            String roomTypeId,
            long dateUtc,
            long nowUtc,
            BookingStatus status) {

        return (int) bookings.values().stream()
                .filter(booking -> hotelId.equals(booking.getHotelId()))
                .filter(booking -> roomTypeId.equals(booking.getRoomTypeId()))
                .filter(booking -> booking.getBookingStatus() == status)
                .filter(booking -> dateUtc >= booking.getCheckInDateUtc()
                        && dateUtc < booking.getCheckOutDateUtc())
                .filter(booking -> status != BookingStatus.HELD || booking.getHoldExpiresAt() > nowUtc)
                .count();
    }

    public int countConfirmedBookings(String hotelId, String roomTypeId, long dateUtc) {
        return countBookings(hotelId, roomTypeId, dateUtc, System.currentTimeMillis(), BookingStatus.CONFIRMED);
    }

    public int countHeldBookings(String hotelId, String roomTypeId, long dateUtc, long nowUtc) {
        return countBookings(hotelId, roomTypeId, dateUtc, nowUtc, BookingStatus.HELD);
    }

    public int countCheckedInBookings(String hotelId, String roomTypeId, long dateUtc) {
        return countBookings(hotelId, roomTypeId, dateUtc, System.currentTimeMillis(), BookingStatus.CHECKED_IN);
    }

    public List<Booking> findExpiredHeldBookings(long nowUtc) {
        return new ArrayList<>(bookings.values().stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.HELD)
                .filter(booking -> booking.getHoldExpiresAt() > 0 && booking.getHoldExpiresAt() <= nowUtc)
                .toList());
    }
}
