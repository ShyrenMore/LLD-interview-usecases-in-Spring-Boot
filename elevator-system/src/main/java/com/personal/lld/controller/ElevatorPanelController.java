package com.personal.lld.controller;

import com.personal.lld.domain.Elevator;
import com.personal.lld.service.BuildingService;
import com.personal.lld.service.ElevatorService;
import com.personal.lld.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elevator-panels")
@RequiredArgsConstructor
@Slf4j
public class ElevatorPanelController {

    private final RequestService requestService;
    private final ElevatorService elevatorService;
    private final BuildingService buildingService;

    @PostMapping("/{elevatorId}/select-floor")
    public void selectFloor(
            @PathVariable String elevatorId,
            @RequestParam int destinationFloor) {

        Elevator elevator =
                elevatorService.findById(elevatorId);

        if (elevator == null) {
            throw new IllegalArgumentException(
                    "Elevator not found: " + elevatorId);
        }

        if (!buildingService.isValidFloor(
                elevator.getBuildingId(),
                destinationFloor)) {

            throw new IllegalArgumentException(
                    "Invalid floor number: " + destinationFloor);
        }

        // Don't create a request if elevator is already on the floor
        if (elevator.getCurrentFloor() == destinationFloor) {

            log.info(
                    "Elevator {} is already on floor {}",
                    elevatorId,
                    destinationFloor);

            return;
        }

        // Check whether elevator can accept internal requests
        if (!elevator.canAcceptInternalRequests()) {

            log.info(
                    "Elevator {} cannot accept new requests in current state: {}",
                    elevatorId,
                    elevator.getStateHandler().getStateName());

            return;
        }

        requestService.createInternalRequest(
                elevatorId,
                destinationFloor);

        log.info(
                "Floor {} selected in elevator {}",
                destinationFloor,
                elevatorId);
    }
}