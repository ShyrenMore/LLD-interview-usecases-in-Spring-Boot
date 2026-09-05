package com.personal.lld.service;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.BookingStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class BookingStateHandler {

    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        add(BookingStatus.CREATED, BookingStatus.HELD, BookingStatus.CANCELLED);
        add(BookingStatus.HELD, BookingStatus.CONFIRMED, BookingStatus.CANCELLED);
        add(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CANCELLED);
        add(BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);
    }

    private BookingStateHandler() {
    }

    private static void add(BookingStatus from, BookingStatus... destinations) {
        VALID_TRANSITIONS.put(from, EnumSet.of(destinations[0], destinations.length > 1 ? destinations[1] : destinations[0],
                destinations.length > 2 ? destinations[2] : destinations[0]));
    }

    public static boolean canTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        return currentStatus == newStatus
                || VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus);
    }

    public static void transition(Booking booking, BookingStatus newStatus) {
        if (!canTransition(booking.getBookingStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Invalid state transition: " + booking.getBookingStatus() + " -> " + newStatus);
        }
        booking.setBookingStatus(newStatus);
    }

    public static void requireStatus(Booking booking, BookingStatus expectedStatus) {
        if (booking.getBookingStatus() != expectedStatus) {
            throw new IllegalStateException(
                    "Booking must be in " + expectedStatus + " status. Current status: "
                            + booking.getBookingStatus());
        }
    }

    public static boolean canCancel(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.CREATED
                || booking.getBookingStatus() == BookingStatus.HELD
                || booking.getBookingStatus() == BookingStatus.CONFIRMED;
    }

    public static boolean canCheckIn(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.CONFIRMED;
    }

    public static boolean canCheckOut(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.CHECKED_IN;
    }

    public static boolean canInitiateTransaction(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.CREATED;
    }

    public static boolean countsInInventory(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.HELD
                || booking.getBookingStatus() == BookingStatus.CONFIRMED
                || booking.getBookingStatus() == BookingStatus.CHECKED_IN;
    }
}
