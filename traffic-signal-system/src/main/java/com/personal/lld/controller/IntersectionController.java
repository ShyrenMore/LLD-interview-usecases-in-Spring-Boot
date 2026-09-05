package com.personal.lld.controller;

import com.personal.lld.domain.Intersection;
import com.personal.lld.domain.IntersectionCreationPayload;
import com.personal.lld.domain.IntersectionCycle;
import com.personal.lld.domain.TrafficLight;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.service.IntersectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/api/intersections")
@RequiredArgsConstructor
public class IntersectionController {

    private final IntersectionService intersectionService;

    @PostConstruct
    public void init() {
        log.info("IntersectionController initialized");
    }


    @PostMapping
    public ResponseEntity<String> createIntersection(@RequestBody IntersectionCreationPayload payload) {
        log.info("Creating intersection: {} (ID: {})", payload.name(), payload.id());
        intersectionService.createIntersection(payload.id(), payload.name());
        return ResponseEntity.ok("Intersection created successfully: " + payload.name());
    }

    @GetMapping("/{intersectionId}")
    public ResponseEntity<Intersection> getIntersection(@PathVariable int intersectionId) {
        log.info("Getting intersection: {}", intersectionId);
        Intersection intersection = intersectionService.getIntersection(intersectionId);

        if (intersection != null) {
            return ResponseEntity.ok(intersection);
        } else {
            log.warn("Intersection not found: {}", intersectionId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{intersectionId}/cycle/start")
    public ResponseEntity<String> startCycle(@PathVariable int intersectionId) {
        log.info("Starting cycle for intersection: {}", intersectionId);
        intersectionService.startAutomaticCycle(intersectionId);
        return ResponseEntity.ok("Cycle started for intersection: " + intersectionId);
    }

    @GetMapping("/{intersectionId}/status")
    public ResponseEntity<String> displayStatus(@PathVariable int intersectionId) {
        log.info("Displaying status for intersection: {}", intersectionId);
        Intersection intersection = intersectionService.getIntersection(intersectionId);

        if (intersection != null) {
            log.info("=== Intersection Status ===");
            log.info("ID: {}", intersection.getId());
            log.info("Name: {}", intersection.getName());
            log.info("Emergency Mode: {}", intersection.isEmergencyMode());
            log.info("Cycle Paused: {}", intersection.isCyclePaused());
            log.info("Paused Phase: {}", intersectionService.getPausedPhase(intersectionId));

            log.info("Traffic Light States:");
            for (Direction direction : Direction.values()) {
                TrafficLight light = intersection.getTrafficLight(direction);
                if (light != null) {
                    log.info("  {}: {}", direction, light.getCurrentStateName());
                }
            }

            IntersectionCycle cycle = intersectionService.getCycle(intersectionId);
            if (cycle != null) {
                log.info("Current Phase: {}", cycle.getCurrentPhase());
                log.info("Phase Start Time: {}", cycle.getPhaseStartTime());
            }
            log.info("========================");

            return ResponseEntity.ok("Status logged and retrieved successfully for intersection: " + intersectionId);
        } else {
            log.warn("Intersection not found: {}", intersectionId);
            return ResponseEntity.notFound().build();
        }
    }
}