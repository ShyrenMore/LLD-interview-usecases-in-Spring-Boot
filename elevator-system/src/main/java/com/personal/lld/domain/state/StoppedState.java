package com.personal.lld.domain.state;

import com.personal.lld.domain.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoppedState implements ElevatorStateHandler {
    public void openDoors(Elevator e) {
        e.setState(ElevatorState.DOORS_OPENING);
        e.setStateHandler(new DoorsOpeningState());
        log.info("Elevator {} opening doors at {}", e.getId(), e.getCurrentFloor());
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
        return "STOPPED";
    }
}
