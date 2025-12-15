package com.personal.lld.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class PricingRule {
    private UUID id;
    private Vehicle.VehicleType vehicleType;
    private double ratePerHour;
    private double flatRate;

    public PricingRule(Vehicle.VehicleType vehicleType, double ratePerHour, double flatRate) {
        this.id = UUID.randomUUID();
        this.vehicleType = vehicleType;
        this.ratePerHour = ratePerHour;
        this.flatRate = flatRate;
    }

    public void updateRates(double ratePerHour, double flatRate) {
        this.ratePerHour = ratePerHour;
        this.flatRate = flatRate;
    }

    public void updateFlatRate(double flatRate) {
        this.flatRate = flatRate;
    }

    public void updateHourlyRate(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

}
