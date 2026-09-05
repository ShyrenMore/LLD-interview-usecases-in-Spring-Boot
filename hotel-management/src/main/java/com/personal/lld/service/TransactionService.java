package com.personal.lld.service;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.BookingStatus;
import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.repository.BookingRepository;
import com.personal.lld.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final long HOLD_TTL_MS = 15 * 60 * 1000L;

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final InventoryService inventoryService;

    public Transaction initiateTransaction(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        BookingStateHandler.requireStatus(booking, BookingStatus.CREATED);

        DateRange range = new DateRange(
                booking.getCheckInDateUtc(),
                booking.getCheckOutDateUtc());

        if (!inventoryService.checkAvailability(
                booking.getHotelId(),
                booking.getRoomTypeId(),
                range,
                1)) {
            throw new IllegalStateException("No availability for the requested dates");
        }

        BookingStateHandler.transition(booking, BookingStatus.HELD);

        long now = System.currentTimeMillis();
        booking.setHoldExpiresAt(now + HOLD_TTL_MS);
        bookingRepository.save(booking);

        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction(
                transactionId,
                bookingId,
                booking.getTotalAmountMinor(),
                "USD",
                TransactionStatus.PENDING,
                "PG_REF_" + transactionId,
                now,
                null,
                null);

        return transactionRepository.save(transaction);
    }

    public void handleCallback(String providerRef, TransactionStatus status) {
        Transaction transaction = transactionRepository.findByProviderRef(providerRef)
                .orElseThrow(() ->
                        new IllegalArgumentException("Transaction not found for providerRef: " + providerRef));

        if (transaction.getStatus() == TransactionStatus.COMPLETED
                || transaction.getStatus() == TransactionStatus.FAILED) {
            return;
        }

        Booking booking = bookingRepository.findById(transaction.getBookingId())
                .orElseThrow(() -> new IllegalStateException("Booking not found for transaction"));

        if (status == TransactionStatus.COMPLETED) {
            if (booking.getBookingStatus() != BookingStatus.HELD) {
                throw new IllegalStateException(
                        "Booking is not HELD while payment completed: " + booking.getBookingStatus());
            }

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(System.currentTimeMillis());

            BookingStateHandler.transition(booking, BookingStatus.CONFIRMED);
            booking.setPaymentStatus(TransactionStatus.COMPLETED);
            booking.setHoldExpiresAt(0);
        } else if (status == TransactionStatus.FAILED) {
            if (booking.getBookingStatus() == BookingStatus.HELD) {
                BookingStateHandler.transition(booking, BookingStatus.CANCELLED);
            }

            transaction.setStatus(TransactionStatus.FAILED);
            booking.setPaymentStatus(TransactionStatus.FAILED);
            booking.setHoldExpiresAt(0);
        } else {
            throw new IllegalArgumentException("Unsupported callback status: " + status);
        }

        transactionRepository.save(transaction);
        bookingRepository.save(booking);

        log.info("Processed payment callback {} -> {}", providerRef, status);
    }

    public Transaction issueRefund(String bookingId, long amountMinor) {
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        String refundTransactionId = UUID.randomUUID().toString();

        Transaction refundTransaction = new Transaction(
                refundTransactionId,
                bookingId,
                amountMinor,
                "USD",
                TransactionStatus.REFUNDED,
                "REFUND_" + refundTransactionId,
                System.currentTimeMillis(),
                null,
                System.currentTimeMillis());

        // TODO: Integrate Payment Gateway and persist provider refund response.
        return transactionRepository.save(refundTransaction);
    }
}
