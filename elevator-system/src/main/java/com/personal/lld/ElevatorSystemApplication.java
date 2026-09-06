package com.personal.lld;

import com.personal.lld.controller.ElevatorController;
import com.personal.lld.controller.ElevatorPanelController;
import com.personal.lld.controller.FloorPanelController;
import com.personal.lld.domain.Building;
import com.personal.lld.domain.Elevator;
import com.personal.lld.domain.strategy.LoadBalancingStrategy;
import com.personal.lld.service.BuildingService;
import com.personal.lld.service.DispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ElevatorSystemApplication implements CommandLineRunner {

    private final ElevatorController elevatorController;
    private final ElevatorPanelController elevatorPanelController;
    private final FloorPanelController floorPanelController;

    private final BuildingService buildingService;
    private final DispatcherService dispatcherService;

    public static void main(String[] args) {
        SpringApplication.run(
                ElevatorSystemApplication.class,
                args
        );
    }

    @Override
    public void run(String... args) {

        try {
            log.info("=== ELEVATOR SYSTEM SIMULATION ===");

            // 1. Create building
            Building building = buildingService.createBuilding(
                    "Tech Tower",
                    1,
                    10,
                    3
            );

            String buildingId = building.getId();

            log.info(
                    "Created building: {} (Floors: {}-{})",
                    building.getName(),
                    building.getMinFloor(),
                    building.getMaxFloor()
            );

            // 2. Create elevators
            Elevator elevator1 =
                    elevatorController.createElevator(
                            buildingId,
                            8
                    );

            Elevator elevator2 =
                    elevatorController.createElevator(
                            buildingId,
                            8
                    );

            Elevator elevator3 =
                    elevatorController.createElevator(
                            buildingId,
                            8
                    );

            log.info(
                    "Created 3 elevators with capacity 8 each"
            );

            log.info(
                    "Elevator IDs: {}, {}, {}",
                    elevator1.getId(),
                    elevator2.getId(),
                    elevator3.getId()
            );

            // 3. Set elevator selection strategy
            dispatcherService.setElevatorSelectionStrategy(
                    new LoadBalancingStrategy()
            );

            log.info(
                    "Set elevator selection strategy to Load Balancing"
            );

            // 4. Start elevator system
            elevatorController.startElevatorSystem(
                    buildingId
            );

            sleep(1000);

            // 5. Simulate external requests
            log.info(
                    "=== SIMULATING EXTERNAL REQUESTS ==="
            );

            floorPanelController.pressUpButton(
                    3,
                    buildingId
            );

            floorPanelController.pressUpButton(
                    7,
                    buildingId
            );

            floorPanelController.pressDownButton(
                    9,
                    buildingId
            );

            floorPanelController.pressUpButton(
                    2,
                    buildingId
            );

            sleep(3000);

            // 6. Simulate internal requests
            log.info(
                    "=== SIMULATING INTERNAL REQUESTS ==="
            );

            elevatorPanelController.selectFloor(
                    elevator1.getId(),
                    5
            );

            elevatorPanelController.selectFloor(
                    elevator1.getId(),
                    8
            );

            elevatorPanelController.selectFloor(
                    elevator2.getId(),
                    4
            );

            elevatorPanelController.selectFloor(
                    elevator3.getId(),
                    6
            );

            sleep(5000);

            // 7. Test requests during pre-maintenance
            log.info(
                    "=== TESTING REQUESTS DURING PRE-MAINTENANCE ==="
            );

            elevatorPanelController.selectFloor(
                    elevator1.getId(),
                    9
            );

            floorPanelController.pressUpButton(
                    4,
                    buildingId
            );

            elevatorPanelController.selectFloor(
                    elevator2.getId(),
                    10
            );

            sleep(3000);

            // 8. Test full maintenance mode
            log.info(
                    "=== TESTING FULL MAINTENANCE MODE ==="
            );

            elevatorController.setElevatorMaintenance(
                    elevator2.getId(),
                    true
            );

            elevatorPanelController.selectFloor(
                    elevator2.getId(),
                    7
            );

            sleep(2000);

            // 9. Try system request during maintenance
            log.info(
                    "=== TESTING SYSTEM REQUEST DURING MAINTENANCE ==="
            );

            floorPanelController.pressUpButton(
                    6,
                    buildingId
            );

            sleep(3000);

            // 10. Exit maintenance
            log.info(
                    "=== EXITING MAINTENANCE MODE ==="
            );

            elevatorController.setElevatorMaintenance(
                    elevator2.getId(),
                    false
            );

            sleep(1000);

            // 11. Stop system
            log.info(
                    "=== STOPPING SYSTEM GRACEFULLY ==="
            );

            elevatorController.stopElevatorSystem(
                    buildingId
            );

            // Try request after system stop
            floorPanelController.pressUpButton(
                    8,
                    buildingId
            );

            log.info(
                    "=== SIMULATION COMPLETED ==="
            );

        } catch (Exception e) {
            log.error(
                    "Error during elevator simulation",
                    e
            );
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Simulation interrupted");
        }
    }
}