package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FareEstimateResponse {
    private final long estimatedFare;
    private final double distanceKm;
    private final long durationSec;
    private final String currency;
}
