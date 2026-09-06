package com.personal.lld.domain;

import lombok.Getter;

@Getter
public class RideStatusResponse {
    private final String rideId;
    private final RideStatus status;
    private final String driverId;
    private final String driverName;
    private final Location driverLocation;
    private final long estimatedFare;
    private final long updatedAt;

    public RideStatusResponse(
            String rideId,
            RideStatus status,
            String driverId,
            String driverName,
            Location driverLocation,
            long estimatedFare,
            long updatedAt) {

        this.rideId = rideId;
        this.status = status;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverLocation = driverLocation;
        this.estimatedFare = estimatedFare;
        this.updatedAt = updatedAt;
    }

    // Retained for compatibility with the original implementation.
    public RideStatusResponse(
            String rideId,
            RideStatus status,
            String driverId,
            String driverName,
            Location driverLocation,
            long estimatedFare,
            long legacyFare,
            long updatedAt) {

        this(
                rideId,
                status,
                driverId,
                driverName,
                driverLocation,
                estimatedFare,
                updatedAt);
    }
}
