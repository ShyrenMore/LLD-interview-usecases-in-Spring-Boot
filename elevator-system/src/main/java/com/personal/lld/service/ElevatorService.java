package com.personal.lld.service;

import java.util.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.personal.lld.domain.*;
import com.personal.lld.domain.state.*;
import com.personal.lld.repository.ElevatorRepository;

@Service
@RequiredArgsConstructor
public class ElevatorService {
    private final ElevatorRepository repo;
    private final RequestService requestService;

    public Elevator createElevator(String b, int c) {
        return repo.save(new Elevator(b, c));
    }

    public Elevator findById(String id) {
        return repo.findById(id).orElse(null);
    }

    public List<Elevator> getAvailableElevators(String b) {
        return repo.findAvailableElevators(b);
    }

    public List<Elevator> getAllElevators(String b) {
        return repo.findByBuilding(b);
    }

    public List<Elevator> getElevatorsByBuilding(String b) {
        return repo.findByBuilding(b);
    }

    public void updateElevatorState(String id, ElevatorState s) {
        repo.findById(id).ifPresent(e -> {
            e.setState(s);
            repo.save(e);
        });
    }

    public void updateElevatorFloor(String id, int f) {
        repo.findById(id).ifPresent(e -> {
            e.setCurrentFloor(f);
            repo.save(e);
        });
    }

    public void setMaintenanceMode(String id, boolean on) {
        repo.findById(id).ifPresent(e -> {
            if (on) {
                if (!requestService.getPendingRequestsForElevator(id).isEmpty() || !requestService.getAssignedRequestsForElevator(id).isEmpty() || e.getState() == ElevatorState.MOVING)
                    e.setStateHandler(new PreMaintenanceState());
                else e.enterMaintenance();
            } else e.exitMaintenance();
            repo.save(e);
        });
    }

    public void checkMaintenanceTransition(String id) {
        repo.findById(id).ifPresent(e -> {
            if (e.isPreparingForMaintenance() && requestService.getPendingRequestsForElevator(id).isEmpty() && requestService.getAssignedRequestsForElevator(id).isEmpty()) {
                e.setActive(false);
                e.setState(ElevatorState.MAINTENANCE);
                e.setStateHandler(new MaintenanceState());
                repo.save(e);
            }
        });
    }
}
