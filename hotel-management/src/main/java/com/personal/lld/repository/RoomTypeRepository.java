package com.personal.lld.repository;

import com.personal.lld.domain.RoomType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomTypeRepository {
    private final Map<String, RoomType> roomTypes = new ConcurrentHashMap<>();

    public RoomType save(RoomType roomType) {
        roomTypes.put(roomType.getId(), roomType);
        return roomType;
    }

    public Optional<RoomType> findById(String roomTypeId) {
        return Optional.ofNullable(roomTypes.get(roomTypeId));
    }

    public List<RoomType> findByHotel(String hotelId) {
        return roomTypes.values().stream()
                .filter(RoomType::isActive)
                .filter(roomType -> hotelId.equals(roomType.getHotelId()))
                .toList();
    }
}
