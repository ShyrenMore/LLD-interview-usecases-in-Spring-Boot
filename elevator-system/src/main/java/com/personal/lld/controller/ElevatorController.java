package com.personal.lld.controller;

import com.personal.lld.domain.Elevator;
import com.personal.lld.service.BuildingService;
import com.personal.lld.service.DispatcherService;
import com.personal.lld.service.ElevatorService;
import com.personal.lld.service.MovementService;
import com.personal.lld.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elevators")
@RequiredArgsConstructor
@Slf4j
public class ElevatorController {

    private final ElevatorService elevatorService;
    private final BuildingService buildingService;
    private final RequestService requestService;
    private final DispatcherService dispatcherService;
    private final MovementService movementService;

    @PostMapping
    public Elevator createElevator(
            @RequestParam String buildingId,
            @RequestParam int capacity) {

        if (!buildingService.buildingExists(buildingId)) {
            throw new IllegalArgumentException(
                    "Building not found: " + buildingId);
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Elevator capacity must be positive: " + capacity);
        }

        return elevatorService.createElevator(buildingId, capacity);
    }

    @PostMapping("/{elevatorId}/move")
    public void moveElevator(
            @PathVariable String elevatorId,
            @RequestParam int targetFloor) {

        Elevator elevator = elevatorService.findById(elevatorId);

        if (elevator == null) {
            throw new IllegalArgumentException(
                    "Elevator not found: " + elevatorId);
        }

        if (!buildingService.isValidFloor(
                elevator.getBuildingId(),
                targetFloor)) {

            throw new IllegalArgumentException(
                    "Invalid floor number: " + targetFloor);
        }

        requestService.createInternalRequest(
                elevatorId,
                targetFloor);

        log.info(
                "Move request created for elevator {} to floor {}",
                elevatorId,
                targetFloor);
    }

    @PostMapping("/{elevatorId}/maintenance")
    public void setElevatorMaintenance(
            @PathVariable String elevatorId,
            @RequestParam boolean maintenance) {

        elevatorService.setMaintenanceMode(
                elevatorId,
                maintenance);

        log.info(
                "Elevator {} maintenance mode: {}",
                elevatorId,
                maintenance);
    }

    @PostMapping("/buildings/{buildingId}/start")
    public void startElevatorSystem(
            @PathVariable String buildingId) {

        if (!buildingService.buildingExists(buildingId)) {
            throw new IllegalArgumentException(
                    "Building not found: " + buildingId);
        }

        if (!buildingService.isSystemRunning(buildingId)) {
            movementService.startElevatorSystem(buildingId);

            log.info(
                    "Elevator system started for building: {}",
                    buildingId);
        } else {
            log.info(
                    "Elevator system is already running for building: {}",
                    buildingId);
        }
    }

    @PostMapping("/buildings/{buildingId}/stop")
    public void stopElevatorSystem(
            @PathVariable String buildingId) {

        if (!buildingService.buildingExists(buildingId)) {
            throw new IllegalArgumentException(
                    "Building not found: " + buildingId);
        }

        if (buildingService.isSystemRunning(buildingId)) {
            movementService.stopElevatorSystem(buildingId);

            log.info(
                    "Elevator system stopped for building: {}",
                    buildingId);
        } else {
            log.info(
                    "Elevator system is not running for building: {}",
                    buildingId);
        }
    }
}