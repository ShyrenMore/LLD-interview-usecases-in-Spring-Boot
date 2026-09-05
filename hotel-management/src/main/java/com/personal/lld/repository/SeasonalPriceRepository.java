package com.personal.lld.repository;

import com.personal.lld.domain.SeasonalPrice;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SeasonalPriceRepository {
    private final Map<String, SeasonalPrice> prices = new ConcurrentHashMap<>();

    public SeasonalPrice upsert(SeasonalPrice price) {
        prices.put(buildKey(price.getHotelId(), price.getRoomTypeId(), price.getDateUtc()), price);
        return price;
    }

    public Optional<SeasonalPrice> findByKey(String hotelId, String roomTypeId, long dateUtc) {
        return Optional.ofNullable(prices.get(buildKey(hotelId, roomTypeId, dateUtc)));
    }

    private String buildKey(String hotelId, String roomTypeId, long dateUtc) {
        return hotelId + ":" + roomTypeId + ":" + dateUtc;
    }
}
