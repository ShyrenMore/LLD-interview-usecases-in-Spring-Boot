package com.personal.lld.domain.state;

import com.personal.lld.domain.*;

public class DoorsClosingState implements ElevatorStateHandler {
    public void openDoors(Elevator e) {
        e.setState(ElevatorState.DOORS_OPENING);
        e.setStateHandler(new DoorsOpeningState());
    }

    public void closeDoors(Elevator e) {
    }

    public void enterMaintenance(Elevator e) {
        e.setActive(false);
        e.setState(ElevatorState.MAINTENANCE);
        e.setStateHandler(new MaintenanceState());
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
        return "DOORS_CLOSING";
    }
}
