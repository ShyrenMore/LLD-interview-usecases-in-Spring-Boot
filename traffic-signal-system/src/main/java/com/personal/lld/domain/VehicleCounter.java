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
public class VehicleCounter {

    private Direction direction;
    private int count;
    private long lastUpdate;

    public VehicleCounter(Direction direction) {
        this.direction = direction;
        this.count = 0;
        this.lastUpdate = System.currentTimeMillis();
        log.info("Vehicle counter created for direction: {}", direction);
    }

    public void setCount(int count) {
        this.count = count;
        this.lastUpdate = System.currentTimeMillis();
        log.info("Vehicle count updated for {}: {}", direction, count);
    }

    public void incrementCount() {
        this.count++;
        this.lastUpdate = System.currentTimeMillis();
        log.info("Vehicle count incremented for {}: {}", direction, count);
    }

    public void resetCount() {
        this.count = 0;
        this.lastUpdate = System.currentTimeMillis();
        log.info("Vehicle count reset for {}", direction);
    }
}