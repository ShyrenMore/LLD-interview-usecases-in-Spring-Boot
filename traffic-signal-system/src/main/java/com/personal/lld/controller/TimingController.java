package com.personal.lld.controller;

import com.personal.lld.domain.SignalTiming;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.service.TimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/api/timings")
@RequiredArgsConstructor
public class TimingController {

    private final TimingService timingService;

    @PostConstruct
    public void init() {
        log.info("TimingController initialized");
    }

    // DTO record to capture update timing payloads
    public record TimingUpdatePayload(int greenDuration) {}

    // DTO record to capture dynamic toggle payloads
    public record DynamicTogglePayload(boolean enable) {}

    @PutMapping("/{intersectionId}/{direction}")
    public ResponseEntity<String> setSignalTiming(
            @PathVariable int intersectionId,
            @PathVariable Direction direction,
            @RequestBody TimingUpdatePayload payload) {

        log.info("Setting signal timing for intersection {}, direction {}: Y={}s, G={}s",
                intersectionId, direction, SignalTiming.YELLOW_DURATION, payload.greenDuration());

        timingService.setSignalTiming(intersectionId, direction, payload.greenDuration());
        return ResponseEntity.ok("Signal timing updated successfully for " + direction);
    }

    @PutMapping("/{intersectionId}/{direction}/dynamic")
    public ResponseEntity<String> enableDynamicTiming(
            @PathVariable int intersectionId,
            @PathVariable Direction direction,
            @RequestBody DynamicTogglePayload payload) {

        log.info("Enabling dynamic timing for intersection {}, direction {}: {}",
                intersectionId, direction, payload.enable());

        timingService.enableDynamicTiming(intersectionId, direction, payload.enable());
        return ResponseEntity.ok("Dynamic timing status updated for " + direction);
    }

    @GetMapping("/{intersectionId}/{direction}")
    public ResponseEntity<SignalTiming> getSignalTiming(
            @PathVariable int intersectionId,
            @PathVariable Direction direction) {

        log.info("Getting signal timing for intersection {}, direction {}", intersectionId, direction);
        SignalTiming timing = timingService.getSignalTiming(intersectionId, direction);

        if (timing != null) {
            return ResponseEntity.ok(timing);
        } else {
            log.warn("Signal timing not found for intersection {}, direction {}", intersectionId, direction);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{intersectionId}/{direction}/adjust")
    public ResponseEntity<String> adjustTimingBasedOnTraffic(
            @PathVariable int intersectionId,
            @PathVariable Direction direction) {

        log.info("Adjusting timing based on traffic for intersection {}, direction {}", intersectionId, direction);
        timingService.adjustTimingBasedOnTraffic(intersectionId, direction);

        // TODO: FUTURE ENHANCEMENT - This will be implemented in future versions
        return ResponseEntity.ok("Timing adjustment triggered for intersection " + intersectionId);
    }

    @GetMapping("/{intersectionId}/status")
    public ResponseEntity<String> displayTimingStatus(@PathVariable int intersectionId) {
        log.info("=== Timing Status for Intersection {} ===", intersectionId);

        for (Direction direction : Direction.values()) {
            SignalTiming timing = timingService.getSignalTiming(intersectionId, direction);
            if (timing != null) {
                log.info("{}: Y={}s, G={}s, Dynamic={}",
                        direction, timing.getYellowDuration(), timing.getGreenDuration(), timing.isDynamic());
            } else {
                log.info("{}: No timing configured", direction);
            }
        }
        log.info("===============================================");

        return ResponseEntity.ok("Timing status logged successfully for intersection: " + intersectionId);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateTiming(@RequestParam int greenDuration) {
        log.info("Validating timing parameters: Y={}s, G={}s", SignalTiming.YELLOW_DURATION, greenDuration);
        timingService.validateTiming(greenDuration);
        return ResponseEntity.ok("Timing validation completed successfully");
    }

    @GetMapping("/optimal-duration")
    public ResponseEntity<Integer> calculateOptimalGreenDuration(@RequestParam int vehicleCount) {
        log.info("Calculating optimal green duration for {} vehicles", vehicleCount);
        int optimalDuration = timingService.calculateOptimalGreenDuration(vehicleCount);
        return ResponseEntity.ok(optimalDuration);
    }
}