package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ride {
    private String id;
    private String riderId;
    private String driverId;
    private Location pickupLocation;
    private Location dropoffLocation;
    private RideStatus status;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private long estimatedFare;
    private double estimatedDistanceKm;
    private double actualDistanceKm;
    private long estimatedDurationSec;
    private long actualDurationSec;
    private long requestedAt;
    private long assignedAt;
    private long acceptedAt;
    private long startedAt;
    private long completedAt;
    private long cancelledAt;
    private String cancellationReason;

    public Ride(
            String id,
            String riderId,
            Location pickupLocation,
            Location dropoffLocation,
            PaymentType paymentType,
            long requestedAt) {

        this.id = id;
        this.riderId = riderId;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.paymentType = paymentType;
        this.requestedAt = requestedAt;
        this.status = RideStatus.REQUESTED;
        this.paymentStatus = PaymentStatus.NONE;
    }
}
