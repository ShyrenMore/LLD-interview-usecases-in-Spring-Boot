package com.personal.lld.service;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.DriverStatus;
import com.personal.lld.domain.NotificationMessage;
import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideStatus;
import com.personal.lld.domain.strategy.DriverMatchingStrategy;
import com.personal.lld.repository.DriverRepository;
import com.personal.lld.repository.RideRepository;
import com.personal.lld.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final int DEFAULT_MAX_RESULTS = 3;
    private static final long DRIVER_RESPONSE_TIMEOUT_MS = 30000;

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final DriverMatchingStrategy matchingStrategy;
    private final LockService lockService;
    private final NotificationService notificationService;

    public Optional<Driver> matchDriver(Ride ride) {

        if (ride.getStatus() != RideStatus.REQUESTED) {
            return Optional.empty();
        }

        List<Driver> availableDrivers =
                driverRepository.findByStatus(
                        DriverStatus.ONLINE);

        List<Driver> suggestions =
                matchingStrategy.findMatchingDrivers(
                        ride.getPickupLocation(),
                        availableDrivers,
                        DEFAULT_MAX_RESULTS);

        if (suggestions.isEmpty()) {
            return Optional.empty();
        }

        for (Driver driver : suggestions) {

            String lockKey =
                    "driver_lock_" + driver.getId();

            boolean acquired =
                    lockService.acquire(
                            lockKey,
                            200);

            if (!acquired) {
                continue;
            }

            try {
                Driver latestDriver =
                        driverRepository.findById(
                                driver.getId())
                                .orElse(driver);

                if (latestDriver.getStatus()
                        != DriverStatus.ONLINE) {
                    continue;
                }

                latestDriver.setCurrentLocation(
                        driver.getCurrentLocation());

                driverRepository.save(latestDriver);

                ride.setStatus(RideStatus.REQUESTED);

                notificationService.send(
                        new NotificationMessage(
                                latestDriver.getId(),
                                "New ride request",
                                "Ride " + ride.getId()
                                        + " is available. "
                                        + "Please accept or decline."));

                long startTime =
                        System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime
                        < DRIVER_RESPONSE_TIMEOUT_MS
                        || ride.getStatus()
                        == RideStatus.DENIED) {

                    Ride currentRide =
                            rideRepository.findById(
                                    ride.getId())
                                    .orElse(ride);

                    if (currentRide.getStatus()
                            == RideStatus.ACCEPTED
                            && latestDriver.getId()
                            .equals(currentRide.getDriverId())) {

                        return Optional.of(latestDriver);
                    }

                    if (currentRide.getStatus()
                            == RideStatus.CANCELLED) {

                        return Optional.empty();
                    }

                    if (currentRide.getDriverId() != null
                            && !currentRide.getDriverId()
                            .equals(latestDriver.getId())) {

                        break;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                Ride finalRide =
                        rideRepository.findById(
                                ride.getId())
                                .orElse(ride);

                if (finalRide.getStatus()
                        == RideStatus.ACCEPTED
                        && latestDriver.getId()
                        .equals(finalRide.getDriverId())) {

                    return Optional.of(latestDriver);
                }

                if (finalRide.getStatus()
                        == RideStatus.CANCELLED) {

                    return Optional.empty();
                }

            } finally {
                lockService.release(lockKey);
            }
        }

        return Optional.empty();
    }

    public void releaseDriver(String driverId) {

        if (driverId == null) {
            return;
        }

        driverRepository.findById(driverId)
                .ifPresent(driver -> {
                    driver.setStatus(DriverStatus.ONLINE);
                    driverRepository.save(driver);
                });
    }
}
