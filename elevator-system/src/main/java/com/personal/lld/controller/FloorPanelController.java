package com.personal.lld.controller;

import com.personal.lld.domain.Direction;
import com.personal.lld.domain.ExternalRequest;
import com.personal.lld.service.BuildingService;
import com.personal.lld.service.DispatcherService;
import com.personal.lld.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/floor-panels")
@RequiredArgsConstructor
@Slf4j
public class FloorPanelController {

    private final RequestService requestService;
    private final BuildingService buildingService;
    private final DispatcherService dispatcherService;

    @PostMapping("/{buildingId}/{floorNumber}/up")
    public void pressUpButton(
            @PathVariable int floorNumber,
            @PathVariable String buildingId) {

        if (!buildingService.isValidFloor(
                buildingId,
                floorNumber)) {

            throw new IllegalArgumentException(
                    "Invalid floor number: " + floorNumber);
        }

        if (!buildingService.isSystemRunning(buildingId)) {

            log.info(
                    "Elevator system is not running. " +
                            "UP request rejected for building {}",
                    buildingId);

            return;
        }

        ExternalRequest request =
                requestService.createExternalRequest(
                        floorNumber,
                        Direction.UP,
                        buildingId);

        dispatcherService.queueExternalRequest(request);

        log.info(
                "UP button pressed on floor {} in building {}",
                floorNumber,
                buildingId);
    }

    @PostMapping("/{buildingId}/{floorNumber}/down")
    public void pressDownButton(
            @PathVariable int floorNumber,
            @PathVariable String buildingId) {

        if (!buildingService.isValidFloor(
                buildingId,
                floorNumber)) {

            throw new IllegalArgumentException(
                    "Invalid floor number: " + floorNumber);
        }

        if (!buildingService.isSystemRunning(buildingId)) {

            log.info(
                    "Elevator system is not running. " +
                            "DOWN request rejected for building {}",
                    buildingId);

            return;
        }

        ExternalRequest request =
                requestService.createExternalRequest(
                        floorNumber,
                        Direction.DOWN,
                        buildingId);

        dispatcherService.queueExternalRequest(request);

        log.info(
                "DOWN button pressed on floor {} in building {}",
                floorNumber,
                buildingId);
    }
}