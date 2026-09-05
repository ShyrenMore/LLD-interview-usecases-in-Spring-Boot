package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
    private boolean available;
    private long totalPriceMinor;
    private double averagePricePerNight;
    private List<NightlyPrice> nightlyPrices;
}
