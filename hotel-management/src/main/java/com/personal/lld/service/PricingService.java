package com.personal.lld.service;

import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.NightlyPrice;
import com.personal.lld.domain.RoomType;
import com.personal.lld.domain.SeasonalPrice;
import com.personal.lld.repository.RoomTypeRepository;
import com.personal.lld.repository.SeasonalPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.personal.lld.service.InventoryService.MILLIS_PER_DAY;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final RoomTypeRepository roomTypeRepository;
    private final SeasonalPriceRepository seasonalPriceRepository;

    public List<NightlyPrice> rateStay(String hotelId, String roomTypeId, DateRange range) {
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return List.of();
        }

        long basePrice = roomTypeOpt.get().getBasePriceMinor();
        List<NightlyPrice> nightlyPrices = new ArrayList<>();

        for (long dateUtc = range.getCheckInDateUtc();
             dateUtc < range.getCheckOutDateUtc();
             dateUtc += MILLIS_PER_DAY) {

            long price = seasonalPriceRepository.findByKey(hotelId, roomTypeId, dateUtc)
                    .map(SeasonalPrice::getPriceMinor)
                    .orElse(basePrice);

            nightlyPrices.add(new NightlyPrice(dateUtc, price));
        }

        return nightlyPrices;
    }

    public long computeTotal(List<NightlyPrice> nightlyPrices) {
        return nightlyPrices.stream()
                .mapToLong(NightlyPrice::getPriceMinor)
                .sum();
    }

    public double computeAveragePricePerNight(List<NightlyPrice> nightlyPrices) {
        if (nightlyPrices.isEmpty()) {
            return 0.0;
        }
        return (double) computeTotal(nightlyPrices) / nightlyPrices.size();
    }
}
