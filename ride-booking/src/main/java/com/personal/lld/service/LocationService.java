package com.personal.lld.service;

import com.personal.lld.domain.Location;
import com.personal.lld.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public void updateDriverLocation(
            String driverId,
            Location location) {

        locationRepository.saveLocation(
                driverId,
                location);
    }

    public Location getDriverLocation(String driverId) {
        return locationRepository.getLatestLocation(driverId);
    }

    public double calculateDistanceKm(
            Location from,
            Location to) {

        if (from == null || to == null) {
            return 0d;
        }

        double latDiff =
                from.getLatitude() - to.getLatitude();

        double lonDiff =
                from.getLongitude() - to.getLongitude();

        return Math.sqrt(
                latDiff * latDiff
                        + lonDiff * lonDiff) * 111;
    }

    public long estimateDurationSec(
            double distanceKm) {

        double avgSpeedKmh = 35;
        double hours = distanceKm / avgSpeedKmh;

        return (long) (hours * 3600);
    }
}
