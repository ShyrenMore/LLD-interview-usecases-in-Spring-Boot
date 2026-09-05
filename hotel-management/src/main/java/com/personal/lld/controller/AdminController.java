package com.personal.lld.controller;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.CancellationPolicy;
import com.personal.lld.domain.Hotel;
import com.personal.lld.domain.RoomType;
import com.personal.lld.domain.SeasonalPrice;
import com.personal.lld.service.BookingService;
import com.personal.lld.repository.CancellationPolicyRepository;
import com.personal.lld.repository.HotelRepository;
import com.personal.lld.repository.RoomTypeRepository;
import com.personal.lld.repository.SeasonalPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SeasonalPriceRepository seasonalPriceRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final BookingService bookingService;

    @PostMapping("/hotels")
    public Hotel createOrUpdateHotel(@RequestBody Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @PostMapping("/room-types")
    public RoomType createOrUpdateRoomType(@RequestBody RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @PatchMapping("/hotels/{hotelId}/overbooking")
    public void updateOverbookingPercent(
            @PathVariable String hotelId,
            @RequestParam int percent) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found: " + hotelId));

        if (percent < 0) {
            throw new IllegalArgumentException("Overbooking percent cannot be negative");
        }

        hotel.setDefaultOverbookPercent(percent);
        hotelRepository.save(hotel);
    }

    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/seasonal-prices")
    public SeasonalPrice setSeasonalPrice(
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @RequestParam long dateUtc,
            @RequestParam long priceMinor) {

        SeasonalPrice price = seasonalPriceRepository
                .findByKey(hotelId, roomTypeId, dateUtc)
                .orElseGet(() -> new SeasonalPrice(
                        UUID.randomUUID().toString(),
                        hotelId,
                        roomTypeId,
                        dateUtc,
                        priceMinor,
                        System.currentTimeMillis()));

        price.setPriceMinor(priceMinor);
        return seasonalPriceRepository.upsert(price);
    }

    @PostMapping("/cancellation-policies")
    public CancellationPolicy createOrUpdatePolicy(@RequestBody CancellationPolicy policy) {
        return cancellationPolicyRepository.save(policy);
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public Booking checkIn(
            @PathVariable String bookingId,
            @RequestParam String roomId,
            @RequestParam long checkInTimeUtc) {

        return bookingService.checkIn(bookingId, roomId, checkInTimeUtc);
    }

    @PostMapping("/bookings/{bookingId}/check-out")
    public Booking checkOut(
            @PathVariable String bookingId,
            @RequestParam long checkOutTimeUtc) {

        return bookingService.checkOut(bookingId, checkOutTimeUtc);
    }
}
