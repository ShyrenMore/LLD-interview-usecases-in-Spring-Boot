package com.personal.lld.service;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.DriverStatus;
import com.personal.lld.domain.FareEstimateResponse;
import com.personal.lld.domain.Location;
import com.personal.lld.domain.NotificationMessage;
import com.personal.lld.domain.PaymentStatus;
import com.personal.lld.domain.PaymentType;
import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideRequest;
import com.personal.lld.domain.RideStatus;
import com.personal.lld.domain.RideStatusResponse;
import com.personal.lld.domain.Rider;
import com.personal.lld.domain.state.RideStateMachine;
import com.personal.lld.repository.DriverRepository;
import com.personal.lld.repository.RideRepository;
import com.personal.lld.repository.RiderRepository;
import com.personal.lld.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final MatchingService matchingService;
    private final PricingService pricingService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final LocationService locationService;
    private final LockService lockService;
    private final RideStateMachine rideStateMachine;

    public Ride requestRide(RideRequest request) {

        validateRequest(request);

        double distanceKm = Math.max(
                0.5d,
                locationService.calculateDistanceKm(
                        request.getPickupLocation(),
                        request.getDropoffLocation()));

        long durationSec = Math.max(
                300L,
                locationService.estimateDurationSec(
                        distanceKm));

        long estimatedFare =
                pricingService.estimateFare(
                        request.getPickupLocation(),
                        request.getDropoffLocation(),
                        distanceKm,
                        durationSec)
                .getEstimatedFare();

        Ride ride = new Ride(
                UUID.randomUUID().toString(),
                request.getRiderId(),
                request.getPickupLocation(),
                request.getDropoffLocation(),
                request.getPaymentType(),
                Instant.now().toEpochMilli());

        ride.setEstimatedDistanceKm(distanceKm);
        ride.setEstimatedDurationSec(durationSec);
        ride.setEstimatedFare(estimatedFare);

        ride.setPaymentStatus(
                request.getPaymentType()
                        == PaymentType.PRE_PAYMENT
                        ? PaymentStatus.PENDING
                        : PaymentStatus.NONE);

        rideRepository.save(ride);

        if (request.getPaymentType()
                == PaymentType.PRE_PAYMENT) {

            paymentService.initiatePayment(ride);

        } else {
            startMatching(ride);
        }

        return ride;
    }

    public FareEstimateResponse estimateFare(
            Location pickup,
            Location dropoff) {

        double distanceKm = Math.max(
                0.5d,
                locationService.calculateDistanceKm(
                        pickup,
                        dropoff));

        long durationSec = Math.max(
                300L,
                locationService.estimateDurationSec(
                        distanceKm));

        return pricingService.estimateFare(
                pickup,
                dropoff,
                distanceKm,
                durationSec);
    }

    public Ride handlePaymentCallback(
            String transactionId,
            PaymentStatus status) {

        Ride ride =
                paymentService.handlePaymentCallback(
                        transactionId,
                        status);

        if (status == PaymentStatus.COMPLETED) {
            startMatching(ride);

        } else if (status == PaymentStatus.FAILED) {

            rideStateMachine.transition(
                    ride,
                    RideStatus.CANCELLED);

            ride.setCancelledAt(
                    Instant.now().toEpochMilli());

            rideRepository.save(ride);
        }

        return ride;
    }

    public RideStatusResponse getRideStatus(String rideId) {

        Ride ride =
                rideRepository.findById(rideId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ride not found"));

        Driver driver =
                ride.getDriverId() != null
                        ? driverRepository.findById(
                                ride.getDriverId())
                        .orElse(null)
                        : null;

        return new RideStatusResponse(
                ride.getId(),
                ride.getStatus(),
                driver != null ? driver.getId() : null,
                driver != null ? driver.getName() : null,
                driver != null
                        ? driver.getCurrentLocation()
                        : null,
                ride.getEstimatedFare(),
                Instant.now().toEpochMilli());
    }

    public void driverAccept(
            String rideId,
            String driverId) {

        withRideLock(rideId, () -> {

            Ride ride =
                    rideRepository.findById(rideId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Ride not found"));

            if (ride.getStatus()
                    == RideStatus.REQUESTED) {

                ride.setDriverId(driverId);

                ride.setAssignedAt(
                        Instant.now().toEpochMilli());

                rideStateMachine.transition(
                        ride,
                        RideStatus.ASSIGNED);
            }

            if (!driverId.equals(ride.getDriverId())) {
                throw new IllegalStateException(
                        "Driver not assigned to ride");
            }

            driverRepository.findById(driverId)
                    .ifPresent(driver -> {
                        driver.setStatus(
                                DriverStatus.ON_RIDE);

                        driverRepository.save(driver);
                    });

            rideStateMachine.transition(
                    ride,
                    RideStatus.ACCEPTED);

            ride.setAcceptedAt(
                    Instant.now().toEpochMilli());

            rideRepository.save(ride);

            notificationService.send(
                    new NotificationMessage(
                            ride.getRiderId(),
                            "Driver accepted",
                            "Driver " + driverId
                                    + " accepted ride "
                                    + rideId));
        });
    }

    public void driverDecline(
            String rideId,
            String driverId) {

        withRideLock(rideId, () -> {

            Ride ride =
                    rideRepository.findById(rideId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Ride not found"));

            if (ride.getStatus()
                    == RideStatus.REQUESTED) {
                return;
            }
        });
    }

    public void startRide(
            String rideId,
            String driverId) {

        withRideLock(rideId, () -> {

            Ride ride =
                    rideRepository.findById(rideId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Ride not found"));

            if (!driverId.equals(ride.getDriverId())) {
                throw new IllegalStateException(
                        "Driver mismatch");
            }

            rideStateMachine.transition(
                    ride,
                    RideStatus.IN_PROGRESS);

            ride.setStartedAt(
                    Instant.now().toEpochMilli());

            rideRepository.save(ride);
        });
    }

    public void completeRide(
            String rideId,
            String driverId) {

        withRideLock(rideId, () -> {

            Ride ride =
                    rideRepository.findById(rideId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Ride not found"));

            if (!driverId.equals(ride.getDriverId())) {
                throw new IllegalStateException(
                        "Driver mismatch");
            }

            rideStateMachine.transition(
                    ride,
                    RideStatus.COMPLETED);

            ride.setCompletedAt(
                    Instant.now().toEpochMilli());

            double distanceKm = Math.max(
                    0.5d,
                    locationService.calculateDistanceKm(
                            ride.getPickupLocation(),
                            ride.getDropoffLocation()));

            ride.setActualDistanceKm(distanceKm);

            long durationSec = Math.max(
                    ride.getEstimatedDurationSec(),
                    locationService.estimateDurationSec(
                            distanceKm));

            ride.setActualDurationSec(durationSec);

            if (ride.getPaymentType()
                    == PaymentType.POST_PAYMENT) {

                ride.setPaymentStatus(
                        PaymentStatus.COMPLETED);

            } else if (ride.getPaymentStatus()
                    != PaymentStatus.COMPLETED) {

                ride.setPaymentStatus(
                        PaymentStatus.COMPLETED);
            }

            rideRepository.save(ride);

            matchingService.releaseDriver(driverId);

            notificationService.send(
                    new NotificationMessage(
                            ride.getRiderId(),
                            "Trip completed",
                            "Fare charged: "
                                    + ride.getEstimatedFare()));
        });
    }

    public void cancelRide(
            String rideId,
            String reason) {

        withRideLock(rideId, () -> {

            Ride ride =
                    rideRepository.findById(rideId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Ride not found"));

            if (ride.getStatus()
                    == RideStatus.COMPLETED
                    || ride.getStatus()
                    == RideStatus.CANCELLED) {
                return;
            }

            if (ride.getDriverId() != null) {
                matchingService.releaseDriver(
                        ride.getDriverId());
            }

            rideStateMachine.transition(
                    ride,
                    RideStatus.CANCELLED);

            ride.setCancelledAt(
                    Instant.now().toEpochMilli());

            ride.setCancellationReason(reason);

            rideRepository.save(ride);
        });
    }

    private void startMatching(Ride ride) {

        boolean matched =
                matchingService.matchDriver(ride)
                        .isPresent();

        if (!matched) {
            log.warn(
                    "No drivers available for ride. rideId={}",
                    ride.getId());
        }
    }

    private void validateRequest(RideRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Ride request is required");
        }

        riderRepository.findById(
                        request.getRiderId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rider not found"));

        Location pickup =
                request.getPickupLocation();

        Location drop =
                request.getDropoffLocation();

        if (pickup == null || drop == null) {
            throw new IllegalArgumentException(
                    "Pickup and dropoff locations are required");
        }

        if (Math.abs(
                pickup.getLatitude()
                        - drop.getLatitude()) < 0.0001
                && Math.abs(
                pickup.getLongitude()
                        - drop.getLongitude()) < 0.0001) {

            throw new IllegalArgumentException(
                    "Pickup and dropoff cannot be the same");
        }
    }

    private void withRideLock(
            String rideId,
            Runnable runnable) {

        String lockKey =
                "ride_lock_" + rideId;

        boolean acquired =
                lockService.acquire(
                        lockKey,
                        500);

        if (!acquired) {
            throw new IllegalStateException(
                    "Could not acquire lock for ride "
                            + rideId);
        }

        try {
            runnable.run();
        } finally {
            lockService.release(lockKey);
        }
    }
}
