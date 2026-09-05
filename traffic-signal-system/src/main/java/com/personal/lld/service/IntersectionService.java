package com.personal.lld.service;


import com.personal.lld.domain.Intersection;
import com.personal.lld.domain.IntersectionCycle;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.repository.IntersectionRepository;
import com.personal.lld.repository.TimingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntersectionService {

    private final IntersectionRepository intersectionRepository;
    private final TimingRepository timingRepository;

    @PostConstruct
    public void init() {
        log.info("IntersectionService initialized");
    }

    public void createIntersection(int id, String name) {
        if (intersectionRepository.exists(id)) {
            log.warn("Intersection with ID {} already exists", id);
            return;
        }

        Intersection intersection = new Intersection(id, name);
        intersectionRepository.save(intersection);

        // Initialize default signal timings
        timingRepository.initializeDefaultTimings(id);

        // Create cycle for the intersection
        IntersectionCycle cycle = new IntersectionCycle(id);
        intersectionRepository.updateCycle(id, cycle);

        startAutomaticCycle(id);
        log.info("Intersection created successfully: {} (ID: {})", name, id);
    }

    public Intersection getIntersection(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection == null) {
            log.warn("Intersection not found: {}", intersectionId);
        }
        return intersection;
    }

    public void startAutomaticCycle(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection == null) {
            log.warn("Cannot start cycle: Intersection not found: {}", intersectionId);
            return;
        }

        IntersectionCycle cycle = intersectionRepository.getCycle(intersectionId);
        if (cycle == null) {
            cycle = new IntersectionCycle(intersectionId);
            intersectionRepository.updateCycle(intersectionId, cycle);
        }

        cycle.setPaused(false);
        intersection.setCyclePaused(false);

        log.info("Automatic cycle started for intersection: {}", intersectionId);
        // In a real implementation, this would start a timer/scheduler
        log.debug("TODO: Implement timer-based automatic cycling");
    }

    public void pauseCycle(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection == null) {
            log.warn("Cannot pause cycle: Intersection not found: {}", intersectionId);
            return;
        }

        IntersectionCycle cycle = intersectionRepository.getCycle(intersectionId);
        if (cycle != null) {
            cycle.setPaused(true);
            intersection.setCyclePaused(true);
            log.info("Cycle paused for intersection: {}", intersectionId);
        }
    }

    public void resumeCycle(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection == null) {
            log.warn("Cannot resume cycle: Intersection not found: {}", intersectionId);
            return;
        }

        IntersectionCycle cycle = intersectionRepository.getCycle(intersectionId);
        if (cycle != null) {
            cycle.setPaused(false);
            intersection.setCyclePaused(false);
            log.info("Cycle resumed for intersection: {}", intersectionId);
        }
    }

    public IntersectionCycle getCycle(int intersectionId) {
        return intersectionRepository.getCycle(intersectionId);
    }

    public void setAllSignalsToRed(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            intersection.setAllSignalsToRed();
            log.info("All signals set to RED for intersection: {}", intersectionId);
        }
    }

    public void emergencySetAllSignalsToRed(int intersectionId) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            // Use emergency transition for each direction
            for (Direction direction : Direction.values()) {
                intersection.emergencyTransitionToRed(direction);
            }
            log.info("Emergency: All signals transitioned to RED for intersection {}", intersectionId);
        }
    }

    public void setSignalToGreen(int intersectionId, Direction direction) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            intersection.setSignalToGreen(direction);
            log.info("Signal set to GREEN for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public void setSignalToYellow(int intersectionId, Direction direction) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            intersection.setSignalToYellow(direction);
            log.info("Signal set to YELLOW for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public void setSignalToRed(int intersectionId, Direction direction) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            intersection.setSignalToRed(direction);
            log.info("Signal set to RED for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public void setSignalToOff(int intersectionId, Direction direction) {
        Intersection intersection = intersectionRepository.findById(intersectionId);
        if (intersection != null) {
            intersection.setSignalToOff(direction);
            log.info("Signal set to OFF for intersection {}, direction {}", intersectionId, direction);
        }
    }

    public int getPausedPhase(int intersectionId) {
        IntersectionCycle cycle = intersectionRepository.getCycle(intersectionId);
        return cycle != null ? cycle.getPausedAtPhase() : 0;
    }
}