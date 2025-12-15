package com.personal.lld.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class ParkingSlot {
    private UUID id;
    private Vehicle.VehicleType slotType;
    private boolean isOccupied;
    private int floorNumber;

    public ParkingSlot(Vehicle.VehicleType slotType, int floorNumber) {
        this.id = UUID.randomUUID();
        this.slotType = slotType;
        this.isOccupied = false;
        this.floorNumber = floorNumber;
    }


}
