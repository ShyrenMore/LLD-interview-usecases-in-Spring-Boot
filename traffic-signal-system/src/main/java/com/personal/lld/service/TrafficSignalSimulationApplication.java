package com.personal.lld.service;

import com.personal.lld.controller.EmergencyController;
import com.personal.lld.controller.IntersectionController;
import com.personal.lld.controller.TimingController;
import com.personal.lld.controller.TrafficController;
import com.personal.lld.domain.IntersectionCreationPayload;
import com.personal.lld.domain.enums.Direction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSignalSimulationApplication implements CommandLineRunner {

    // Spring automatically injects all controllers thanks to @RequiredArgsConstructor & @SpringBootApplication
    private final IntersectionController intersectionController;
    private final EmergencyController emergencyController;
    private final TrafficController trafficController;
    private final TimingController timingController;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Traffic Signal System Simulation Started ===");

        log.info("\n--- Creating Intersection ---");
        intersectionController.createIntersection(IntersectionCreationPayload.builder()
                .id(1)
                .name("Main Street & Oak Avenue")
                .build());

        log.info("\n--- Setting Signal Timings ---");
        timingController.setSignalTiming(1, Direction.NORTH, new TimingController.TimingUpdatePayload(30));
        timingController.setSignalTiming(1, Direction.SOUTH, new TimingController.TimingUpdatePayload(30));
        timingController.setSignalTiming(1, Direction.EAST, new TimingController.TimingUpdatePayload(15));
        timingController.setSignalTiming(1, Direction.WEST, new TimingController.TimingUpdatePayload(15));

        log.info("\n--- Enabling Dynamic Timing ---");
        timingController.enableDynamicTiming(1, Direction.NORTH, new TimingController.DynamicTogglePayload(true));
        timingController.enableDynamicTiming(1, Direction.SOUTH, new TimingController.DynamicTogglePayload(true));

        log.info("\n--- Starting Automatic Cycle ---");
        intersectionController.startCycle(1);

        log.info("\n--- Displaying Initial Status ---");
        intersectionController.displayStatus(1);
        timingController.displayTimingStatus(1);

        log.info("\n--- Updating Traffic Counts ---");
        trafficController.updateVehicleCount(Direction.NORTH, new TrafficController.VehicleCountPayload(15));
        trafficController.updateVehicleCount(Direction.SOUTH, new TrafficController.VehicleCountPayload(8));
        trafficController.updateVehicleCount(Direction.EAST, new TrafficController.VehicleCountPayload(3));
        trafficController.updateVehicleCount(Direction.WEST, new TrafficController.VehicleCountPayload(12));

        log.info("\n--- Displaying Traffic Status ---");
        trafficController.displayTrafficStatus();

        log.info("\n--- Adjusting Timing Based on Traffic ---");
        timingController.adjustTimingBasedOnTraffic(1, Direction.NORTH);
        timingController.adjustTimingBasedOnTraffic(1, Direction.SOUTH);

        log.info("\n--- Requesting Emergency ---");
        emergencyController.requestEmergency(new EmergencyController.EmergencyPayload(1, Direction.EAST, 30));

        log.info("\n--- Displaying Status During Emergency ---");
        intersectionController.displayStatus(1);
        emergencyController.getEmergencyStatus(1);

        log.info("\n--- Ending Emergency ---");
        emergencyController.endEmergency(1);

        log.info("\n--- Displaying Final Status ---");
        intersectionController.displayStatus(1);
        timingController.displayTimingStatus(1);

        log.info("\n--- Simulation Complete ---");
    }
}