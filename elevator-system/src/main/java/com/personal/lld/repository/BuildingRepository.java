package com.personal.lld.repository;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Repository;
import com.personal.lld.domain.Building;

@Repository
public class BuildingRepository {
    private final Map<String, Building> store = new ConcurrentHashMap<>();

    public Building save(Building b) {
        store.put(b.getId(), b);
        return b;
    }

    public Optional<Building> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
