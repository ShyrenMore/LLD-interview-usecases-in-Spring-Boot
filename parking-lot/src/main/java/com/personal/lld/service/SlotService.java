package com.personal.lld.service;

import com.personal.lld.domain.ParkingSlot;
import com.personal.lld.domain.Vehicle;
import com.personal.lld.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    public Optional<ParkingSlot> allocateSlot(Vehicle.VehicleType vehicleType) {
        System.out.println("[SERVICE] Allocating slot for vehicle type: " + vehicleType);

        Optional<ParkingSlot> slot = slotRepository.allocateSlot(vehicleType);
        if (slot.isPresent()) {
            System.out.println("[SERVICE] Slot allocated successfully: " + slot.get().getId());
        } else {
            System.out.println("[SERVICE] No available slots for vehicle type: " + vehicleType);
        }

        return slot;
    }

    public void releaseSlot(UUID slotId) {
        System.out.println("[SERVICE] Releasing slot: " + slotId);
        slotRepository.releaseSlot(slotId);
        System.out.println("[SERVICE] Slot released successfully: " + slotId);
    }

    public ParkingSlot createSlot(Vehicle.VehicleType slotType, int floorNumber) {
        ParkingSlot slot = new ParkingSlot(slotType, floorNumber);
        slotRepository.save(slot);
        return slot;
    }

    public long getAvailableSlotCount(Vehicle.VehicleType vehicleType) {
        return slotRepository.findAvailableSlots(vehicleType).size();
    }
}
