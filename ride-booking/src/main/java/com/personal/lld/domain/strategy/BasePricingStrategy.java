package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Location;
import org.springframework.stereotype.Component;

@Component
public class BasePricingStrategy implements PricingStrategy {

    private final long baseFareMinor = 2000;
    private final long perKmMinor = 800;
    private final long perMinuteMinor = 200;

    @Override
    public long calculateFare(
            Location pickup,
            Location dropoff,
            double distanceKm,
            long durationSec) {

        long distanceComponent =
                (long) (distanceKm * perKmMinor);

        long timeComponent =
                (durationSec / 60) * perMinuteMinor;

        return Math.max(
                baseFareMinor,
                baseFareMinor
                        + distanceComponent
                        + timeComponent);
    }
}
