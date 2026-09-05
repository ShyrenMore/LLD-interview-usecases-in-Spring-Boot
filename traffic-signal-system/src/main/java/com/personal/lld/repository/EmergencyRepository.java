package com.personal.lld.repository;

import com.personal.lld.domain.EmergencyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
public class EmergencyRepository {

    // Thread-safe map since Spring beans are singletons and can be accessed concurrently
    private final Map<Integer, EmergencyRequest> emergencyRequests = new ConcurrentHashMap<>();

    // Thread-safe ID generation
    private final AtomicInteger nextId = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        log.info("EmergencyRepository initialized");
    }

    public void save(EmergencyRequest request) {
        emergencyRequests.put(request.getId(), request);
        log.info("Emergency request saved: {}", request.getId());
    }

    public EmergencyRequest getActiveEmergency(int intersectionId) {
        // Utilizing Java Streams for a cleaner, functional approach
        EmergencyRequest activeRequest = emergencyRequests.values().stream()
                .filter(request -> request.getIntersectionId() == intersectionId && request.isActive())
                .findFirst()
                .orElse(null);

        if (activeRequest != null) {
            log.info("Active emergency found for intersection: {}", intersectionId);
        } else {
            log.info("No active emergency found for intersection: {}", intersectionId);
        }

        return activeRequest;
    }

    public void updateStatus(int requestId, boolean isActive) {
        EmergencyRequest request = emergencyRequests.get(requestId);
        if (request != null) {
            request.setActive(isActive);
            log.info("Emergency request status updated: {} -> {}", requestId, isActive);
        } else {
            log.warn("Emergency request not found for ID: {}", requestId);
        }
    }

    public int getNextId() {
        return nextId.getAndIncrement();
    }

    public void removeExpiredRequests() {
        boolean removed = emergencyRequests.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (removed) {
            log.info("Expired emergency requests were removed");
        } else {
            log.debug("No expired emergency requests to remove");
        }
    }
}
