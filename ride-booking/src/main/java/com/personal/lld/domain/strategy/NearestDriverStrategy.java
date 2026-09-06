package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.Location;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NearestDriverStrategy implements DriverMatchingStrategy {

    @Override
    public List<Driver> findMatchingDrivers(
            Location pickup,
            List<Driver> candidates,
            int maxResults) {

        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        driver -> distanceKm(
                                pickup,
                                driver.getCurrentLocation())))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private double distanceKm(Location a, Location b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        double latDiff =
                a.getLatitude() - b.getLatitude();

        double lonDiff =
                a.getLongitude() - b.getLongitude();

        return Math.sqrt(
                latDiff * latDiff
                        + lonDiff * lonDiff) * 111;
    }
}
