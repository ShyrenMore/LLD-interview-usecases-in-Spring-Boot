package com.personal.lld.service;

import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.Hotel;
import com.personal.lld.domain.RoomType;
import com.personal.lld.domain.RoomTypeAvailability;
import com.personal.lld.domain.SearchFilter;
import com.personal.lld.domain.NightlyPrice;
import com.personal.lld.repository.HotelRepository;
import com.personal.lld.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.personal.lld.service.InventoryService.MILLIS_PER_DAY;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

    public List<Hotel> searchHotels(SearchFilter filter) {
        if (filter == null) {
            return List.of();
        }

        List<Hotel> hotels = filter.getCity() != null && filter.getCountry() != null
                ? hotelRepository.findByLocation(filter.getCity(), filter.getCountry())
                : hotelRepository.findAllActive();

        if (filter.getBedType() == null
                && filter.getMinPriceMinor() == null
                && filter.getMaxPriceMinor() == null) {
            return hotels;
        }

        return hotels.stream()
                .filter(hotel -> matchesRoomTypeFilters(hotel.getId(), filter))
                .toList();
    }

    public List<RoomTypeAvailability> getAvailability(String hotelId, DateRange range) {
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel == null || !hotel.isActive()) {
            return List.of();
        }

        List<RoomTypeAvailability> result = new ArrayList<>();

        for (RoomType roomType : roomTypeRepository.findByHotel(hotelId)) {
            if (!isAvailableForRange(hotelId, roomType, range, hotel.getDefaultOverbookPercent())) {
                continue;
            }

            List<NightlyPrice> nightlyPrices = pricingService.rateStay(hotelId, roomType.getId(), range);
            long totalPrice = pricingService.computeTotal(nightlyPrices);
            double average = pricingService.computeAveragePricePerNight(nightlyPrices);

            result.add(new RoomTypeAvailability(
                    roomType.getId(),
                    roomType.getName(),
                    roomType.getCapacity(),
                    roomType.getBedType(),
                    roomType.getAmenities(),
                    true,
                    totalPrice,
                    average,
                    nightlyPrices));
        }

        return result;
    }

    private boolean isAvailableForRange(
            String hotelId,
            RoomType roomType,
            DateRange range,
            int overbookPercent) {

        int overbookAllowed = (int) Math.ceil(
                roomType.getTotalRooms() * overbookPercent / 100.0);

        for (long dateUtc = range.getCheckInDateUtc();
             dateUtc < range.getCheckOutDateUtc();
             dateUtc += MILLIS_PER_DAY) {

            int confirmed = inventoryService.getConfirmedBookingsCount(
                    hotelId, roomType.getId(), dateUtc);
            int held = inventoryService.getHeldBookingsCount(
                    hotelId, roomType.getId(), dateUtc);
            int checkedIn = inventoryService.getCheckedInBookingsCount(
                    hotelId, roomType.getId(), dateUtc);

            int available = roomType.getTotalRooms()
                    + overbookAllowed
                    - confirmed
                    - held
                    - checkedIn;

            if (available < 1) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesRoomTypeFilters(String hotelId, SearchFilter filter) {
        return roomTypeRepository.findByHotel(hotelId).stream()
                .filter(RoomType::isActive)
                .anyMatch(roomType -> {
                    if (filter.getBedType() != null
                            && !filter.getBedType().equalsIgnoreCase(roomType.getBedType())) {
                        return false;
                    }

                    long price = roomType.getBasePriceMinor();

                    if (filter.getMinPriceMinor() != null && price < filter.getMinPriceMinor()) {
                        return false;
                    }

                    if (filter.getMaxPriceMinor() != null && price > filter.getMaxPriceMinor()) {
                        return false;
                    }

                    return true;
                });
    }
}
