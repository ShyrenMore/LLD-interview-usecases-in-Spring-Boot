package com.personal.lld.repository.impl;

import com.personal.lld.domain.Rider;
import com.personal.lld.repository.RiderRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRiderRepository implements RiderRepository {

    private final Map<String, Rider> storage =
            new ConcurrentHashMap<>();

    @Override
    public Optional<Rider> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(Rider rider) {
        storage.put(rider.getId(), rider);
    }
}
