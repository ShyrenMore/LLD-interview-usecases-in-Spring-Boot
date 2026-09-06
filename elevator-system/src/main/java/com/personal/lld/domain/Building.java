package com.personal.lld.domain;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Building {
    private final String id = UUID.randomUUID().toString();
    private final String name;
    private final int minFloor, maxFloor, totalElevators;
    private SystemState systemState = SystemState.STOPPED;

    public Building(String name, int minFloor, int maxFloor, int totalElevators) {
        if (name == null || name.isBlank() || minFloor > maxFloor || totalElevators <= 0)
            throw new IllegalArgumentException("Invalid building configuration");
        this.name = name;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.totalElevators = totalElevators;
    }

    public void setSystemState(SystemState s) {
        systemState = s;
    }

    public boolean isValidFloor(int floor) {
        return floor >= minFloor && floor <= maxFloor;
    }
}
