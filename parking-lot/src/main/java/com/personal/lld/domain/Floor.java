package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Floor {
    private UUID id;
    private int floorNumber;
    private List<ParkingSlot> slots;

    public Floor(int floorNumber) {
        this.id = UUID.randomUUID();
        this.floorNumber = floorNumber;
        this.slots = new ArrayList<>();
    }

    public void addSlot(ParkingSlot slot) {
        slots.add(slot);
    }

    public List<ParkingSlot> getAvailableSlots(Vehicle.VehicleType vehicleType) {
        List<ParkingSlot> availableSlots = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if (slot.getSlotType() == vehicleType && !slot.isOccupied()) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }

    public int getTotalSlots() {
        return slots.size();
    }

    public int getAvailableSlotsCount(Vehicle.VehicleType vehicleType) {
        return getAvailableSlots(vehicleType).size();
    }

}
