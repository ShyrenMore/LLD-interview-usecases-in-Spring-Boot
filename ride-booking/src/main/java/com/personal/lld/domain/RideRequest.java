package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RideRequest {
    private String riderId;
    private Location pickupLocation;
    private Location dropoffLocation;
    private PaymentType paymentType;
}
