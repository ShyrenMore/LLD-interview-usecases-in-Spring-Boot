package com.personal.lld.controller;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.DateRange;
import com.personal.lld.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Booking createBooking(
            @RequestParam String userId,
            @RequestParam String hotelId,
            @RequestParam String roomTypeId,
            @RequestBody DateRange range,
            @RequestParam(defaultValue = "0") long expectedTotalPriceMinor) {

        return bookingService.createBooking(
                userId,
                hotelId,
                roomTypeId,
                range,
                expectedTotalPriceMinor);
    }

    @PostMapping("/{bookingId}/cancel")
    public void cancelBooking(
            @PathVariable String bookingId,
            @RequestParam String userId) {

        bookingService.cancelBooking(bookingId, userId);
    }
}
