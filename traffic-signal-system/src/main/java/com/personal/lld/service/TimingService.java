package com.personal.lld.service;

import com.personal.lld.domain.SignalTiming;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.repository.TimingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimingService {

    private final TimingRepository timingRepository;
    private final TrafficService trafficService;

    @PostConstruct
    public void init() {
        log.info("TimingService initialized");
    }

    public void setSignalTiming(int intersectionId, Direction direction, int greenDuration) {
        // Validate timing parameters
        if (greenDuration < 10) {
            log.warn("Invalid timing parameters: minimum green duration not met (10s)");
            return;
        }

        timingRepository.updateSignalTiming(intersectionId, direction, greenDuration);
        log.info("Signal timing set for intersection {}, direction {}: Y={}s, G={}s",
                intersectionId, direction, SignalTiming.YELLOW_DURATION, greenDuration);
    }

    public void enableDynamicTiming(int intersectionId, Direction direction, boolean enable) {
        timingRepository.enableDynamicTiming(intersectionId, direction, enable);
        log.info("Dynamic timing {} for intersection {}, direction {}",
                (enable ? "enabled" : "disabled"), intersectionId, direction);
    }

    public SignalTiming getSignalTiming(int intersectionId, Direction direction) {
        SignalTiming timing = timingRepository.getSignalTiming(intersectionId, direction);
        if (timing != null) {
            log.info("Signal timing retrieved for intersection {}, direction {}", intersectionId, direction);
        } else {
            log.warn("Signal timing not found for intersection {}, direction {}", intersectionId, direction);
        }
        return timing;
    }

    public void adjustTimingBasedOnTraffic(int intersectionId, Direction direction) {
        // TODO: FUTURE ENHANCEMENT - Implement dynamic timing adjustment
        // This method should:
        // 1. Get current vehicle count for the direction
        // 2. Calculate optimal green duration based on traffic density
        // 3. Update signal timing if significantly different from current
        // 4. Apply changes to next cycle (not current cycle)
        // 5. Consider safety constraints (min/max durations)
        // 6. Log timing changes for analysis
        log.debug("TODO: Dynamic timing adjustment not yet implemented for intersection {}, direction {}",
                intersectionId, direction);
    }

    public int calculateOptimalGreenDuration(int vehicleCount) {
        // TODO: FUTURE ENHANCEMENT - Implement sophisticated timing algorithm
        // This method should:
        // 1. Use machine learning models for traffic prediction
        // 2. Consider historical traffic patterns
        // 3. Factor in time of day, weather, events
        // 4. Balance throughput vs wait times
        // 5. Ensure safety constraints are met
        log.debug("TODO: Optimal duration calculation not yet implemented for {} vehicles", vehicleCount);
        return 30; // Default fallback duration
    }

    public void validateTiming(int greenDuration) {
        if (greenDuration < 10) {
            log.warn("Warning: Green duration too short (minimum 10s)");
        }
        if (greenDuration > 120) {
            log.warn("Warning: Green duration too long (maximum 120s)");
        }

        log.info("Timing validation completed for duration: {}s", greenDuration);
    }
}