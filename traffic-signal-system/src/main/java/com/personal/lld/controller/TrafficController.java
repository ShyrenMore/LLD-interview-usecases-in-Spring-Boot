package com.personal.lld.controller;

import com.personal.lld.domain.enums.Direction;
import com.personal.lld.service.TrafficService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    @PostConstruct
    public void init() {
        log.info("TrafficController initialized");
    }

    // DTO record to capture vehicle count payloads
    public record VehicleCountPayload(int count) {
    }

    @PutMapping("/{direction}")
    public ResponseEntity<String> updateVehicleCount(
            @PathVariable Direction direction,
            @RequestBody VehicleCountPayload payload) {

        log.info("Updating vehicle count for {}: {}", direction, payload.count());
        trafficService.updateVehicleCount(direction, payload.count());

        return ResponseEntity.ok("Vehicle count updated successfully for " + direction);
    }

    @GetMapping("/{direction}")
    public ResponseEntity<Integer> getVehicleCount(@PathVariable Direction direction) {
        log.info("Getting vehicle count for {}", direction);
        int count = trafficService.getVehicleCount(direction);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{direction}/increment")
    public ResponseEntity<String> incrementVehicleCount(@PathVariable Direction direction) {
        log.info("Incrementing vehicle count for {}", direction);
        trafficService.incrementVehicleCount(direction);
        return ResponseEntity.ok("Vehicle count incremented for " + direction);
    }

    @DeleteMapping("/{direction}")
    public ResponseEntity<String> resetVehicleCount(@PathVariable Direction direction) {
        log.info("Resetting vehicle count for {}", direction);
        trafficService.resetVehicleCount(direction);
        return ResponseEntity.ok("Vehicle count reset for " + direction);
    }

    @GetMapping("/status")
    public ResponseEntity<String> displayTrafficStatus() {
        log.info("=== Traffic Status ===");
        for (Direction direction : Direction.values()) {
            int count = trafficService.getVehicleCount(direction);
            log.info("{}: {} vehicles", direction, count);
        }
        log.info("=====================");

        return ResponseEntity.ok("Traffic status logged successfully");
    }

    @PostMapping("/detect")
    public ResponseEntity<String> detectTrafficConditions() {
        log.info("Detecting traffic conditions for all directions");
        for (Direction direction : Direction.values()) {
            trafficService.detectTrafficCondition(direction);
        }
        return ResponseEntity.ok("Traffic condition detection completed across all directions");
    }
}