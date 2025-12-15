package com.personal.lld.repository;

import com.personal.lld.domain.ParkingSlot;
import com.personal.lld.domain.Vehicle;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Repository
public class SlotRepository {
    private Map<UUID, ParkingSlot> slots = new ConcurrentHashMap<>();

    public ParkingSlot save(ParkingSlot slot) {
        slots.put(slot.getId(), slot);
        return slot;
    }

    public Optional<ParkingSlot> findById(UUID slotId) {
        return Optional.ofNullable(slots.get(slotId));
    }

    public List<ParkingSlot> findAvailableSlots(Vehicle.VehicleType vehicleType) {
        return slots.values().stream()
                .filter(slot -> slot.getSlotType() == vehicleType && !slot.isOccupied())
                .collect(Collectors.toList());
    }

    public Optional<ParkingSlot> allocateSlot(Vehicle.VehicleType vehicleType) {
        return slots.values().stream()
                .filter(slot -> slot.getSlotType() == vehicleType && !slot.isOccupied())
                .findFirst()
                .map(slot -> {
                    slot.setOccupied(true);
                    return slot;
                });
    }

    public void releaseSlot(UUID slotId) {
        slots.computeIfPresent(slotId, (id, slot) -> {
            slot.setOccupied(false);
            return slot;
        });
    }

    public Map<Vehicle.VehicleType, Long> getSlotStatistics() {
        return slots.values().stream()
                .collect(Collectors.groupingBy(
                        ParkingSlot::getSlotType,
                        Collectors.counting()
                ));
    }

    public void clear() {
        slots.clear();
    }
}
