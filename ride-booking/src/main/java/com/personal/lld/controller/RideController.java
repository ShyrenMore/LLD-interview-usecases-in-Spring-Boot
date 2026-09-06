package com.personal.lld.controller;

import com.personal.lld.domain.FareEstimateResponse;
import com.personal.lld.domain.Location;
import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideRequest;
import com.personal.lld.domain.RideStatusResponse;
import com.personal.lld.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/fare-estimate")
    public FareEstimateResponse getFareEstimate(
            @RequestBody FareEstimateRequest request) {

        return rideService.estimateFare(
                request.getPickup(),
                request.getDropoff());
    }

    @PostMapping
    public Ride requestRide(
            @RequestBody RideRequest request) {

        return rideService.requestRide(request);
    }

    @GetMapping("/{rideId}")
    public RideStatusResponse getRideStatus(
            @PathVariable String rideId) {

        return rideService.getRideStatus(rideId);
    }

    @PostMapping("/{rideId}/cancel")
    public void cancelRide(
            @PathVariable String rideId,
            @RequestParam String reason) {

        rideService.cancelRide(
                rideId,
                reason);
    }

    public static class FareEstimateRequest {
        private Location pickup;
        private Location dropoff;

        public Location getPickup() {
            return pickup;
        }

        public void setPickup(Location pickup) {
            this.pickup = pickup;
        }

        public Location getDropoff() {
            return dropoff;
        }

        public void setDropoff(Location dropoff) {
            this.dropoff = dropoff;
        }
    }
}
