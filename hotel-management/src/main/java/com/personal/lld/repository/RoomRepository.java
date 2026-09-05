package com.personal.lld.repository;

import com.personal.lld.domain.Room;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomRepository {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room save(Room room) {
        rooms.put(room.getId(), room);
        return room;
    }

    public List<Room> findByHotelAndType(String hotelId, String roomTypeId) {
        return rooms.values().stream()
                .filter(Room::isActive)
                .filter(room -> hotelId.equals(room.getHotelId()) && roomTypeId.equals(room.getRoomTypeId()))
                .toList();
    }
}
