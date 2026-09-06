package com.personal.lld.repository;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Repository;
import com.personal.lld.domain.*;

@Repository
public class InternalRequestRepository {
    private final Map<String, InternalRequest> store = new ConcurrentHashMap<>();

    public InternalRequest save(InternalRequest r) {
        store.put(r.getId(), r);
        return r;
    }

    public Optional<InternalRequest> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<InternalRequest> findPendingByElevator(String id) {
        return store.values().stream().filter(r -> id.equals(r.getElevatorId()) && r.getStatus() == RequestStatus.PENDING).toList();
    }

    public List<InternalRequest> findByElevator(String id) {
        return store.values().stream().filter(r -> id.equals(r.getElevatorId())).toList();
    }
}
