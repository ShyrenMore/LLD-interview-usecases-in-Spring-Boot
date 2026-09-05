package com.personal.lld.repository;
import com.personal.lld.domain.Intersection;
import com.personal.lld.domain.IntersectionCycle;
import com.personal.lld.domain.enums.Direction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
public class IntersectionRepository {

    // Thread-safe maps for singletons handling multiple web requests concurrently
    private final Map<Integer, Intersection> intersections = new ConcurrentHashMap<>();
    private final Map<Integer, IntersectionCycle> cycles = new ConcurrentHashMap<>();

    // Thread-safe ID generation
    private final AtomicInteger nextId = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        log.info("IntersectionRepository initialized");
    }

    public void save(Intersection intersection) {
        intersections.put(intersection.getId(), intersection);
        log.info("Intersection saved: {}", intersection.getId());
    }

    public Intersection findById(int intersectionId) {
        Intersection intersection = intersections.get(intersectionId);
        if (intersection != null) {
            log.info("Intersection found: {}", intersectionId);
        } else {
            log.warn("Intersection not found: {}", intersectionId);
        }
        return intersection;
    }

    public void updateCycle(int intersectionId, IntersectionCycle cycle) {
        cycles.put(intersectionId, cycle);
        log.info("Cycle updated for intersection: {}", intersectionId);
    }

    public IntersectionCycle getCycle(int intersectionId) {
        IntersectionCycle cycle = cycles.get(intersectionId);
        if (cycle != null) {
            log.info("Cycle found for intersection: {}", intersectionId);
        } else {
            log.warn("Cycle not found for intersection: {}", intersectionId);
        }
        return cycle;
    }

    public void updateEmergencyMode(int intersectionId, boolean emergencyMode, Direction direction) {
        Intersection intersection = intersections.get(intersectionId);
        if (intersection != null) {
            intersection.setEmergencyMode(emergencyMode);
            intersection.setEmergencyDirection(direction);
            log.info("Emergency mode updated for intersection: {} (Mode: {}, Direction: {})",
                    intersectionId, emergencyMode, direction);
        } else {
            log.warn("Failed to update emergency mode - Intersection not found: {}", intersectionId);
        }
    }

    public int getNextId() {
        return nextId.getAndIncrement();
    }

    public boolean exists(int intersectionId) {
        return intersections.containsKey(intersectionId);
    }
}