package com.personal.lld.repository;

import com.personal.lld.domain.Hotel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class HotelRepository {
    private final Map<String, Hotel> hotels = new ConcurrentHashMap<>();

    public Hotel save(Hotel hotel) {
        hotels.put(hotel.getId(), hotel);
        return hotel;
    }

    public Optional<Hotel> findById(String hotelId) {
        return Optional.ofNullable(hotels.get(hotelId));
    }

    public List<Hotel> findByLocation(String city, String country) {
        return hotels.values().stream()
                .filter(Hotel::isActive)
                .filter(hotel -> city.equalsIgnoreCase(hotel.getCity()))
                .filter(hotel -> country.equalsIgnoreCase(hotel.getCountry()))
                .toList();
    }

    public List<Hotel> findAllActive() {
        return hotels.values().stream()
                .filter(Hotel::isActive)
                .toList();
    }
}
