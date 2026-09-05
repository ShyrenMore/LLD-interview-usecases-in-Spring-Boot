package com.personal.lld.repository;


import com.personal.lld.domain.VehicleCounter;
import com.personal.lld.domain.enums.Direction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class TrafficRepository {

    // Thread-safe map for concurrent multi-threaded requests
    private final Map<Direction, VehicleCounter> vehicleCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize counters for all directions upon bean creation
        for (Direction direction : Direction.values()) {
            this.vehicleCounters.put(direction, new VehicleCounter(direction));
        }
        log.info("TrafficRepository initialized with default counters for all directions");
    }

    public void updateCount(Direction direction, int count) {
        VehicleCounter counter = vehicleCounters.get(direction);
        if (counter != null) {
            counter.setCount(count);
            log.info("Vehicle count updated for {}: {}", direction, count);
        } else {
            log.warn("Vehicle counter not found for direction: {}", direction);
        }
    }

    public int getCount(Direction direction) {
        VehicleCounter counter = vehicleCounters.get(direction);
        if (counter != null) {
            int currentCount = counter.getCount();
            log.info("Vehicle count retrieved for {}: {}", direction, currentCount);
            return currentCount;
        } else {
            log.warn("Vehicle counter not found for direction: {}", direction);
            return 0;
        }
    }

    public void incrementCount(Direction direction) {
        VehicleCounter counter = vehicleCounters.get(direction);
        if (counter != null) {
            counter.incrementCount();
        } else {
            log.warn("Vehicle counter not found for direction: {}", direction);
        }
    }

    public void resetCount(Direction direction) {
        VehicleCounter counter = vehicleCounters.get(direction);
        if (counter != null) {
            counter.resetCount();
        } else {
            log.warn("Vehicle counter not found for direction: {}", direction);
        }
    }

    public VehicleCounter getCounter(Direction direction) {
        return vehicleCounters.get(direction);
    }
}