package com.personal.lld.service;

import java.util.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.personal.lld.domain.*;
import com.personal.lld.domain.state.*;
import com.personal.lld.domain.strategy.*;

@Service
@RequiredArgsConstructor
public class MovementService {
    private final ElevatorService elevatorService;
    private final RequestService requestService;
    private final BuildingService buildingService;
    private final DispatcherService dispatcher;
    private volatile MovementStrategy strategy = new ScanStrategy();

    public void setMovementStrategy(MovementStrategy s) {
        strategy = Objects.requireNonNull(s);
    }

    public void startElevatorSystem(String b) {
        buildingService.setBuildingSystemState(b, SystemState.RUNNING);
        dispatcher.processPendingRequests(b);
    }

    public void stopElevatorSystem(String b) {
        buildingService.setBuildingSystemState(b, SystemState.STOPPING);
        processAllElevatorMovements(b);
        buildingService.setBuildingSystemState(b, SystemState.STOPPED);
    }

    public void processAllElevatorMovements(String b) {
        for (Elevator e : elevatorService.getAllElevators(b))
            if (e.isActive())
                processElevatorMovement(e.getId(), e);
    }

    public void processElevatorMovement(String id, Elevator e) {
        List<InternalRequest> pending = requestService.getPendingRequestsForElevator(id);
        if (pending.isEmpty()) {
            if (e.isPreparingForMaintenance()) elevatorService.checkMaintenanceTransition(id);
            else {
                e.setDirection(Direction.IDLE);
                e.setState(ElevatorState.STOPPED);
            }
            return;
        }
        List<Integer> path = strategy.calculatePath(e, pending);
        if (path.isEmpty()) return;
        int target = path.get(0);
        e.setDirection(target > e.getCurrentFloor() ? Direction.UP : Direction.DOWN);
        e.setState(ElevatorState.MOVING);
        e.setCurrentFloor(target);
        e.setState(ElevatorState.STOPPED);
        for (InternalRequest r : pending)
            if (r.getDestinationFloor() == target) requestService.completeInternalRequest(r.getId());
        for (ExternalRequest r : requestService.getAssignedRequestsForElevator(id))
            if (r.getFloorNumber() == target) requestService.completeExternalRequest(r.getId());
        e.setDirection(Direction.IDLE);
    }
}
