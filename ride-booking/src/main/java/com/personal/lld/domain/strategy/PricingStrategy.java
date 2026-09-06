package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Location;

public interface PricingStrategy {

    long calculateFare(
            Location pickup,
            Location dropoff,
            double distanceKm,
            long durationSec);
}
