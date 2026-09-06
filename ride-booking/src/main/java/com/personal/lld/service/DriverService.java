package com.personal.lld.service;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.DriverStatus;
import com.personal.lld.domain.Location;
import com.personal.lld.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public void goOnline(String driverId) {
        Driver driver = getDriver(driverId);
        driver.setStatus(DriverStatus.ONLINE);
        driverRepository.save(driver);
    }

    public void goOffline(String driverId) {
        Driver driver = getDriver(driverId);
        driver.setStatus(DriverStatus.OFFLINE);
        driverRepository.save(driver);
    }

    public void updateLocation(
            String driverId,
            Location location) {

        Driver driver = getDriver(driverId);

        driver.setCurrentLocation(location);
        driver.setLastLocationUpdate(
                location.getTimestamp());

        driverRepository.save(driver);
    }

    public Optional<Driver> findById(String driverId) {
        return driverRepository.findById(driverId);
    }

    private Driver getDriver(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Driver not found"));
    }
}
