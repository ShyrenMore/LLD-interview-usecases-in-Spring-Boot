package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
public class IntersectionCycle {

    private int intersectionId;
    private int currentPhase; // 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST
    private boolean isPaused;
    private int pausedAtPhase;
    private long phaseStartTime;
    private long pauseStartTime; // Track when pause started
    private long totalPauseTime; // Track total pause duration

    public IntersectionCycle(int intersectionId) {
        this.intersectionId = intersectionId;
        this.currentPhase = 0;
        this.isPaused = false;
        this.pausedAtPhase = 0;
        this.phaseStartTime = System.currentTimeMillis();
        this.pauseStartTime = 0;
        this.totalPauseTime = 0;
        log.info("Intersection cycle created for intersection: {}", intersectionId);
    }

    // Get elapsed time in current phase (excluding pause time)
    public long getPhaseElapsedTime() {
        if (isPaused) {
            return pauseStartTime - phaseStartTime;
        } else {
            return System.currentTimeMillis() - phaseStartTime - totalPauseTime;
        }
    }

    // Get remaining time for current phase
    public long getPhaseRemainingTime(int phaseDurationSeconds) {
        long elapsed = getPhaseElapsedTime();
        long remaining = (phaseDurationSeconds * 1000L) - elapsed;
        return Math.max(0, remaining);
    }

    // Get total pause time override to account for active pause duration
    public long getTotalPauseTime() {
        if (isPaused) {
            return totalPauseTime + (System.currentTimeMillis() - pauseStartTime);
        }
        return totalPauseTime;
    }

    public void setCurrentPhase(int currentPhase) {
        this.currentPhase = currentPhase;
        this.phaseStartTime = System.currentTimeMillis();
        this.totalPauseTime = 0; // Reset pause time for new phase
        log.info("Phase changed to: {} for intersection {}", currentPhase, intersectionId);
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (paused) {
            this.pausedAtPhase = this.currentPhase;
            this.pauseStartTime = System.currentTimeMillis();
            log.info("Cycle paused at phase: {} (elapsed: {}s) for intersection {}",
                    this.currentPhase, getPhaseElapsedTime() / 1000, intersectionId);
        } else {
            // Calculate total pause time when resuming
            this.totalPauseTime += (System.currentTimeMillis() - pauseStartTime);
            log.info("Cycle resumed from phase: {} (remaining: {}s) for intersection {}",
                    this.pausedAtPhase, getPhaseRemainingTime(30) / 1000, intersectionId);
        }
    }

    public void setPausedAtPhase(int pausedAtPhase) {
        this.pausedAtPhase = pausedAtPhase;
        log.info("Paused phase set to: {} for intersection {}", pausedAtPhase, intersectionId);
    }

    public void nextPhase() {
        this.currentPhase = (this.currentPhase + 1) % 4;
        this.phaseStartTime = System.currentTimeMillis();
        this.totalPauseTime = 0; // Reset for new phase
        log.info("Advanced to next phase: {} for intersection {}", this.currentPhase, intersectionId);
    }

    // Check if current phase is complete
    public boolean isPhaseComplete(int phaseDurationSeconds) {
        return getPhaseElapsedTime() >= (phaseDurationSeconds * 1000L);
    }
}
