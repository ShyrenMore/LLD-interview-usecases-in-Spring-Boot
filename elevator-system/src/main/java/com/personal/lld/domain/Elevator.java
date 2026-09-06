package com.personal.lld.domain;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import com.personal.lld.domain.state.*;

@Getter
public class Elevator {
    private final String id = UUID.randomUUID().toString();
    private final String buildingId;
    private final int capacity;
    private int currentFloor = 1;
    private Direction direction = Direction.IDLE;
    private ElevatorState state = ElevatorState.STOPPED;
    private int currentLoad;
    private boolean active = true;
    @Setter
    private ElevatorStateHandler stateHandler = new StoppedState();

    public Elevator(String buildingId, int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.buildingId = buildingId;
        this.capacity = capacity;
    }

    public void setCurrentFloor(int f) {
        currentFloor = f;
    }

    public void setDirection(Direction d) {
        direction = d;
    }

    public void setState(ElevatorState s) {
        state = s;
    }

    public void setCurrentLoad(int l) {
        if (l < 0 || l > capacity) throw new IllegalArgumentException("Invalid load");
        currentLoad = l;
    }

    public void setActive(boolean a) {
        active = a;
    }

    public boolean isFull() {
        return currentLoad >= capacity;
    }

    public boolean isAvailable() {
        return stateHandler.canAcceptExternalRequests(this);
    }

    public boolean canAcceptInternalRequests() {
        return stateHandler.canAcceptInternalRequests(this);
    }

    public void openDoors() {
        stateHandler.openDoors(this);
    }

    public void closeDoors() {
        stateHandler.closeDoors(this);
    }

    public void enterMaintenance() {
        stateHandler.enterMaintenance(this);
    }

    public void exitMaintenance() {
        stateHandler.exitMaintenance(this);
    }

    public boolean isPreparingForMaintenance() {
        return stateHandler instanceof PreMaintenanceState;
    }
}
