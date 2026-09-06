package com.personal.lld.controller;

import com.personal.lld.domain.Location;
import com.personal.lld.service.DriverService;
import com.personal.lld.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final RideService rideService;

    @PutMapping("/{driverId}/online")
    public void goOnline(@PathVariable String driverId) {
        driverService.goOnline(driverId);
    }

    @PutMapping("/{driverId}/offline")
    public void goOffline(@PathVariable String driverId) {
        driverService.goOffline(driverId);
    }

    @PutMapping("/{driverId}/location")
    public void updateLocation(
            @PathVariable String driverId,
            @RequestBody Location location) {

        driverService.updateLocation(
                driverId,
                location);
    }

    @PostMapping("/{driverId}/rides/{rideId}/accept")
    public void acceptRide(
            @PathVariable String rideId,
            @PathVariable String driverId) {

        rideService.driverAccept(
                rideId,
                driverId);
    }

    @PostMapping("/{driverId}/rides/{rideId}/decline")
    public void declineRide(
            @PathVariable String rideId,
            @PathVariable String driverId) {

        rideService.driverDecline(
                rideId,
                driverId);
    }

    @PostMapping("/{driverId}/rides/{rideId}/start")
    public void startRide(
            @PathVariable String rideId,
            @PathVariable String driverId) {

        rideService.startRide(
                rideId,
                driverId);
    }

    @PostMapping("/{driverId}/rides/{rideId}/complete")
    public void completeRide(
            @PathVariable String rideId,
            @PathVariable String driverId) {

        rideService.completeRide(
                rideId,
                driverId);
    }
}
