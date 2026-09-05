package com.personal.lld.controller;

import com.personal.lld.domain.EmergencyRequest;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostConstruct
    public void init() {
        log.info("EmergencyController initialized");
    }

    // DTO record to capture incoming request payload
    public record EmergencyPayload(int intersectionId, Direction direction, int duration) {
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestEmergency(@RequestBody EmergencyPayload payload) {
        log.info("Emergency request received for intersection: {}, direction: {}, duration: {}s",
                payload.intersectionId(), payload.direction(), payload.duration());

        emergencyService.requestEmergency(payload.intersectionId(), payload.direction(), payload.duration());
        return ResponseEntity.ok("Emergency requested successfully");
    }

    @PostMapping("/{intersectionId}/end")
    public ResponseEntity<String> endEmergency(@PathVariable int intersectionId) {
        log.info("Ending emergency for intersection: {}", intersectionId);

        emergencyService.endEmergency(intersectionId);
        return ResponseEntity.ok("Emergency ended for intersection: " + intersectionId);
    }

    @GetMapping("/{intersectionId}")
    public ResponseEntity<EmergencyRequest> getEmergencyStatus(@PathVariable int intersectionId) {
        log.info("Getting emergency status for intersection: {}", intersectionId);

        EmergencyRequest activeEmergency = emergencyService.getActiveEmergency(intersectionId);

        if (activeEmergency != null) {
            log.info("=== Emergency Status ===");
            log.info("Emergency ID: {}", activeEmergency.getId());
            log.info("Direction: {}", activeEmergency.getDirection());
            log.info("Duration: {}s", activeEmergency.getDuration());
            log.info("Active: {}", activeEmergency.isActive());
            log.info("Request Time: {}", activeEmergency.getRequestTime());
            log.info("Expired: {}", activeEmergency.isExpired());
            log.info("=======================");

            return ResponseEntity.ok(activeEmergency);
        } else {
            log.info("No active emergency for intersection: {}", intersectionId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredEmergencies() {
        log.info("Cleaning up expired emergency requests");

        emergencyService.cleanupExpiredEmergencies();
        return ResponseEntity.ok("Cleanup completed successfully");
    }
}