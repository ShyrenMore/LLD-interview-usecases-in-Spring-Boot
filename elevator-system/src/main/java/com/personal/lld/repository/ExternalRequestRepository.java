package com.personal.lld.repository;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Repository;
import com.personal.lld.domain.*;

@Repository
public class ExternalRequestRepository {
    private final Map<String, ExternalRequest> store = new ConcurrentHashMap<>();

    public ExternalRequest save(ExternalRequest r) {
        store.put(r.getId(), r);
        return r;
    }

    public Optional<ExternalRequest> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<ExternalRequest> findByBuildingAndStatus(String b, RequestStatus s) {
        return store.values().stream().filter(r -> b.equals(r.getBuildingId()) && r.getStatus() == s).toList();
    }

    public List<ExternalRequest> findAll() {
        return new ArrayList<>(store.values());
    }
}
