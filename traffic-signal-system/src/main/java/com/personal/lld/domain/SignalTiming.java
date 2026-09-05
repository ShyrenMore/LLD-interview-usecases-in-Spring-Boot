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
public class SignalTiming {

    private int intersectionId;
    private Direction direction;
    private int greenDuration;
    private boolean isDynamic;

    // Constant yellow duration for safety (3 seconds)
    public static final int YELLOW_DURATION = 3;

    public SignalTiming(int intersectionId, Direction direction) {
        this.intersectionId = intersectionId;
        this.direction = direction;
        this.greenDuration = 45; // Default 45 seconds
        this.isDynamic = false;
        log.info("Signal timing created for intersection {}, direction {}", intersectionId, direction);
    }

    // Lombok provides standard getters and setters, but custom log-enabled methods are preserved
    public int getYellowDuration() {
        return YELLOW_DURATION;
    }

    public void setGreenDuration(int greenDuration) {
        this.greenDuration = greenDuration;
        log.info("Green duration set to {} seconds for {}", greenDuration, direction);
    }

    public void setDynamic(boolean dynamic) {
        this.isDynamic = dynamic;
        log.info("Dynamic timing {} for {}", (dynamic ? "enabled" : "disabled"), direction);
    }

    public void updateTiming(int greenDuration) {
        this.greenDuration = greenDuration;
        log.info("Timing updated for {}: Y={}s, G={}s", direction, YELLOW_DURATION, greenDuration);
    }

    /**
     * Calculate the red duration for this direction based on other directions' green and yellow times
     * Red duration = sum of (green + yellow) for all other directions
     */
    public int calculateRedDuration(SignalTiming[] allTimings) {
        int redDuration = 0;
        for (SignalTiming timing : allTimings) {
            if (timing != null && timing.getDirection() != this.direction) {
                redDuration += timing.getGreenDuration() + YELLOW_DURATION;
            }
        }
        return redDuration;
    }

    /**
     * Get the total cycle time for this direction (green + yellow + calculated red)
     */
    public int getTotalCycleTime(SignalTiming[] allTimings) {
        return greenDuration + YELLOW_DURATION + calculateRedDuration(allTimings);
    }
}