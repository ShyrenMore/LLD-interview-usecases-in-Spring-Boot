package com.personal.lld.repository.impl;

import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideStatus;
import com.personal.lld.repository.RideRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryRideRepository implements RideRepository {

    private final Map<String, Ride> storage =
            new ConcurrentHashMap<>();

    @Override
    public Optional<Ride> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(Ride ride) {
        storage.put(ride.getId(), ride);
    }

    @Override
    public List<Ride> findByRiderId(String riderId) {
        return storage.values().stream()
                .filter(ride -> riderId.equals(ride.getRiderId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ride> findByDriverId(String driverId) {
        if (driverId == null) {
            return new ArrayList<>();
        }

        return storage.values().stream()
                .filter(ride -> driverId.equals(ride.getDriverId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ride> findByStatus(RideStatus status) {
        return storage.values().stream()
                .filter(ride -> ride.getStatus() == status)
                .collect(Collectors.toList());
    }
}
