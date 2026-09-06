package com.personal.lld.domain.state;

import com.personal.lld.domain.*;

public class MovingState implements ElevatorStateHandler {
    public void openDoors(Elevator e) {
    }

    public void closeDoors(Elevator e) {
    }

    public void enterMaintenance(Elevator e) {
        e.setStateHandler(new PreMaintenanceState());
    }

    public void exitMaintenance(Elevator e) {
    }

    public boolean canAcceptExternalRequests(Elevator e) {
        return e.isActive() && !e.isFull();
    }

    public boolean canAcceptInternalRequests(Elevator e) {
        return e.isActive();
    }

    public String getStateName() {
        return "MOVING";
    }
}
