package com.personal.lld.repository;


import com.personal.lld.domain.SignalTiming;
import com.personal.lld.domain.enums.Direction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class TimingRepository {

    // Thread-safe map since Spring beans are singletons accessed concurrently
    private final Map<String, SignalTiming> signalTimings = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("TimingRepository initialized");
    }

    public void saveSignalTiming(SignalTiming timing) {
        String key = timing.getIntersectionId() + "-" + timing.getDirection();
        signalTimings.put(key, timing);
        log.info("Signal timing saved for intersection {}, direction {}",
                timing.getIntersectionId(), timing.getDirection());
    }

    public SignalTiming getSignalTiming(int intersectionId, Direction direction) {
        String key = intersectionId + "-" + direction;
        SignalTiming timing = signalTimings.get(key);
        if (timing != null) {
            log.info("Signal timing found for intersection {}, direction {}", intersectionId, direction);
        } else {
            log.warn("Signal timing not found for intersection {}, direction {}", intersectionId, direction);
        }
        return timing;
    }

    public void updateSignalTiming(int intersectionId, Direction direction, int greenDuration) {
        String key = intersectionId + "-" + direction;
        SignalTiming timing = signalTimings.get(key);
        if (timing != null) {
            timing.updateTiming(greenDuration);
            log.info("Signal timing updated for intersection {}, direction {}", intersectionId, direction);
        } else {
            log.warn("Signal timing not found for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public void enableDynamicTiming(int intersectionId, Direction direction, boolean enable) {
        String key = intersectionId + "-" + direction;
        SignalTiming timing = signalTimings.get(key);
        if (timing != null) {
            timing.setDynamic(enable);
            log.info("Dynamic timing {} for intersection {}, direction {}",
                    (enable ? "enabled" : "disabled"), intersectionId, direction);
        } else {
            log.warn("Signal timing not found for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public void initializeDefaultTimings(int intersectionId) {
        for (Direction direction : Direction.values()) {
            SignalTiming timing = new SignalTiming(intersectionId, direction);
            saveSignalTiming(timing);
        }
        log.info("Default timings initialized for intersection: {}", intersectionId);
    }
}