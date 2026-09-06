package com.personal.lld.simulator;

import com.personal.lld.controller.DriverController;
import com.personal.lld.controller.PaymentController;
import com.personal.lld.controller.RideController;
import com.personal.lld.domain.Driver;
import com.personal.lld.domain.DriverStatus;
import com.personal.lld.domain.Location;
import com.personal.lld.domain.PaymentStatus;
import com.personal.lld.domain.PaymentType;
import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideRequest;
import com.personal.lld.domain.RideStatusResponse;
import com.personal.lld.domain.Rider;
import com.personal.lld.repository.DriverRepository;
import com.personal.lld.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideSharingSimulation implements CommandLineRunner {

    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final RideController rideController;
    private final DriverController driverController;
    private final PaymentController paymentController;

    @Override
    public void run(String... args) {

        log.info("=== RIDE SHARING SYSTEM SIMULATION STARTED ===");

        Rider rider = new Rider(
                "rider-1",
                "Alice Rider",
                "alice@example.com",
                "111-222-3333",
                System.currentTimeMillis());

        riderRepository.save(rider);

        Driver driver = new Driver(
                "driver-1",
                "Bob Driver",
                "bob@example.com",
                "444-555-6666",
                "KA01AB1234",
                "Sedan",
                DriverStatus.ONLINE,
                new Location(
                        37.7749,
                        -122.4194,
                        "Market St",
                        System.currentTimeMillis()),
                System.currentTimeMillis());

        driverRepository.save(driver);

        Location pickup = new Location(
                37.7749,
                -122.4194,
                "Market St",
                System.currentTimeMillis());

        Location drop = new Location(
                37.7840,
                -122.4090,
                "Mission St",
                System.currentTimeMillis());

        // ---------------------------------------------------------
        // PRE_PAYMENT ride
        // ---------------------------------------------------------

        log.info("Requesting ride with PRE_PAYMENT");

        RideRequest prePaymentRequest =
                new RideRequest(
                        rider.getId(),
                        pickup,
                        drop,
                        PaymentType.PRE_PAYMENT);

        Ride ride =
                rideController.requestRide(
                        prePaymentRequest);

        log.info(
                "PRE_PAYMENT ride created. rideId={}, paymentId={}",
                ride.getId(),
                ride.getPaymentId());

        paymentController.handleCallback(
                ride.getPaymentId(),
                PaymentStatus.COMPLETED);

        driverController.acceptRide(
                ride.getId(),
                driver.getId());

        driverController.startRide(
                ride.getId(),
                driver.getId());

        driverController.completeRide(
                ride.getId(),
                driver.getId());

        RideStatusResponse completedStatus =
                rideController.getRideStatus(
                        ride.getId());

        log.info(
                "PRE_PAYMENT ride completed. rideId={}, status={}, fare={}",
                ride.getId(),
                completedStatus.getStatus(),
                completedStatus.getEstimatedFare());

        // ---------------------------------------------------------
        // POST_PAYMENT ride
        // ---------------------------------------------------------

        log.info(
                "Requesting ride with POST_PAYMENT (cash)");

        RideRequest postPaymentRequest =
                new RideRequest(
                        rider.getId(),
                        pickup,
                        drop,
                        PaymentType.POST_PAYMENT);

        Ride cashRide =
                rideController.requestRide(
                        postPaymentRequest);

        driverController.acceptRide(
                cashRide.getId(),
                driver.getId());

        driverController.startRide(
                cashRide.getId(),
                driver.getId());

        driverController.completeRide(
                cashRide.getId(),
                driver.getId());

        RideStatusResponse cashStatus =
                rideController.getRideStatus(
                        cashRide.getId());

        log.info(
                "POST_PAYMENT ride completed. rideId={}, status={}, fare={}",
                cashRide.getId(),
                cashStatus.getStatus(),
                cashStatus.getEstimatedFare());

        log.info("=== RIDE SHARING SYSTEM SIMULATION COMPLETED ===");
    }
}
