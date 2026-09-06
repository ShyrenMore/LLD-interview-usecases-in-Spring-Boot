package com.personal.lld.service;

import com.personal.lld.domain.FareEstimateResponse;
import com.personal.lld.domain.Location;
import com.personal.lld.domain.strategy.PricingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingStrategy pricingStrategy;

    public FareEstimateResponse estimateFare(
            Location pickup,
            Location dropoff,
            double distanceKm,
            long durationSec) {

        long fareMinor =
                pricingStrategy.calculateFare(
                        pickup,
                        dropoff,
                        distanceKm,
                        durationSec);

        return new FareEstimateResponse(
                fareMinor,
                distanceKm,
                durationSec,
                "USD");
    }
}
