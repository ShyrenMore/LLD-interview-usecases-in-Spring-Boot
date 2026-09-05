package com.personal.lld.domain;

import com.personal.lld.domain.enums.Direction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
public class EmergencyRequest {

    private int id;
    private int intersectionId;
    private Direction direction;
    private int duration;
    private boolean isActive;
    private long requestTime;

    public EmergencyRequest(int id, int intersectionId, Direction direction, int duration) {
        this.id = id;
        this.intersectionId = intersectionId;
        this.direction = direction;
        this.duration = duration;
        this.isActive = true;
        this.requestTime = System.currentTimeMillis();
        log.info("Emergency request created: ID={}, Intersection={}, Direction={}, Duration={}s",
                id, intersectionId, direction, duration);
    }

    // Custom setter retained to preserve side-effect logging
    public void setActive(boolean active) {
        this.isActive = active;
        log.info("Emergency request {} {}", id, (active ? "activated" : "deactivated"));
    }

    // Custom setter retained to preserve side-effect logging
    public void setDuration(int duration) {
        this.duration = duration;
        log.info("Emergency request {} duration updated to {}s", id, duration);
    }

    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - requestTime) / 1000L; // Convert to seconds using long literal
        return elapsedTime >= duration;
    }
}