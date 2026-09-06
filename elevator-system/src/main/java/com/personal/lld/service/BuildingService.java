package com.personal.lld.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.personal.lld.domain.*;
import com.personal.lld.repository.BuildingRepository;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository repo;

    public Building createBuilding(String n, int min, int max, int total) {
        return repo.save(new Building(n, min, max, total));
    }

    public boolean isValidFloor(String id, int f) {
        return repo.findById(id).map(b -> b.isValidFloor(f)).orElse(false);
    }

    public boolean buildingExists(String id) {
        return repo.findById(id).isPresent();
    }

    public Building findById(String id) {
        return repo.findById(id).orElse(null);
    }

    public void setBuildingSystemState(String id, SystemState s) {
        repo.findById(id).ifPresent(b -> {
            b.setSystemState(s);
            repo.save(b);
        });
    }

    public boolean isSystemRunning(String id) {
        return repo.findById(id).map(b -> b.getSystemState() == SystemState.RUNNING).orElse(false);
    }
}
