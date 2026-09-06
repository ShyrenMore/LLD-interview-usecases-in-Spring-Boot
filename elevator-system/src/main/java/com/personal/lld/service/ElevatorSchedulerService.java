package com.personal.lld.service;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElevatorSchedulerService {
    private final MovementService movementService;
    private final DispatcherService dispatcher;
    private final BuildingService buildingService;
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    public void startBuildingScheduler(String b) {
        jobs.computeIfAbsent(b, id -> executor.scheduleAtFixedRate(() -> {
            if (buildingService.isSystemRunning(id)) {
                dispatcher.processPendingRequests(id);
                movementService.processAllElevatorMovements(id);
            }
        }, 0, 2, TimeUnit.SECONDS));
    }

    public void stopBuildingScheduler(String b) {
        Optional.ofNullable(jobs.remove(b)).ifPresent(f -> f.cancel(false));
    }

    public boolean isSchedulerRunning(String b) {
        ScheduledFuture<?> f = jobs.get(b);
        return f != null && !f.isDone() && !f.isCancelled();
    }

    public void shutdown() {
        jobs.values().forEach(f -> f.cancel(false));
        jobs.clear();
        executor.shutdown();
    }
}
