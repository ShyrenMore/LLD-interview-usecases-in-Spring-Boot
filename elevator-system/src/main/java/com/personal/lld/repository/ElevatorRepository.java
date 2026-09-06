package com.personal.lld.repository;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Repository;
import com.personal.lld.domain.Elevator;

@Repository
public class ElevatorRepository {
    private final Map<String, Elevator> store = new ConcurrentHashMap<>();

    public Elevator save(Elevator e) {
        store.put(e.getId(), e);
        return e;
    }

    public Optional<Elevator> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Elevator> findByBuilding(String id) {
        return store.values().stream().filter(e -> id.equals(e.getBuildingId())).toList();
    }

    public List<Elevator> findAvailableElevators(String id) {
        return findByBuilding(id).stream().filter(Elevator::isAvailable).toList();
    }
}
