package com.personal.lld.domain.state;

import com.personal.lld.domain.Elevator;

public interface ElevatorStateHandler {
    void openDoors(Elevator e);

    void closeDoors(Elevator e);

    void enterMaintenance(Elevator e);

    void exitMaintenance(Elevator e);

    boolean canAcceptExternalRequests(Elevator e);

    boolean canAcceptInternalRequests(Elevator e);

    String getStateName();
}
