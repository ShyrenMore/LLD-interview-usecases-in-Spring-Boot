package com.personal.lld.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class Vehicle {
    private UUID id;
    private String licensePlate;
    private VehicleType vehicleType;

    public enum VehicleType {
        BIKE, CAR, TRUCK, EV
    }

    public Vehicle(String licensePlate, VehicleType vehicleType) {
        this.id = UUID.randomUUID();
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }
}
