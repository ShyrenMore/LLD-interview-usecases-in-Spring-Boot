package com.personal.lld.domain.state;

import com.personal.lld.domain.*;

public class DoorsOpeningState implements ElevatorStateHandler {
    public void openDoors(Elevator e) {
    }

    public void closeDoors(Elevator e) {
        e.setState(ElevatorState.DOORS_CLOSING);
        e.setStateHandler(new DoorsClosingState());
    }

    public void enterMaintenance(Elevator e) {
        closeDoors(e);
        e.enterMaintenance();
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
        return "DOORS_OPENING";
    }
}
