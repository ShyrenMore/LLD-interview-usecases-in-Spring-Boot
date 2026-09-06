package com.personal.lld.repository.impl;

import com.personal.lld.domain.Location;
import com.personal.lld.repository.LocationRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLocationRepository implements LocationRepository {

    private final Map<String, Location> storage =
            new ConcurrentHashMap<>();

    @Override
    public void saveLocation(
            String driverId,
            Location location) {

        storage.put(driverId, location);
    }

    @Override
    public Location getLatestLocation(String driverId) {
        return storage.get(driverId);
    }
}
