package com.personal.lld.domain.state;

import com.personal.lld.domain.*;

public class PreMaintenanceState implements ElevatorStateHandler {
    public void checkForMaintenanceTransition(Elevator e) {
        e.setActive(false);
        e.setState(ElevatorState.MAINTENANCE);
        e.setStateHandler(new MaintenanceState());
    }

    public void openDoors(Elevator e) {
        e.setState(ElevatorState.DOORS_OPENING);
    }

    public void closeDoors(Elevator e) {
        e.setState(ElevatorState.DOORS_CLOSING);
    }

    public void enterMaintenance(Elevator e) {
        checkForMaintenanceTransition(e);
    }

    public void exitMaintenance(Elevator e) {
        e.setActive(true);
        e.setState(ElevatorState.STOPPED);
        e.setStateHandler(new StoppedState());
    }

    public boolean canAcceptExternalRequests(Elevator e) {
        return false;
    }

    public boolean canAcceptInternalRequests(Elevator e) {
        return false;
    }

    public String getStateName() {
        return "PRE_MAINTENANCE";
    }
}
