package com.personal.lld.service;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.personal.lld.domain.*;
import com.personal.lld.domain.strategy.*;

@Service
@RequiredArgsConstructor
public class DispatcherService {
    private final RequestService requestService;
    private final ElevatorService elevatorService;
    private final BlockingQueue<ExternalRequest> queue = new LinkedBlockingQueue<>();
    private volatile ElevatorSelectionStrategy strategy = new NearestElevatorStrategy();

    public void setElevatorSelectionStrategy(ElevatorSelectionStrategy s) {
        strategy = Objects.requireNonNull(s);
    }

    public void queueExternalRequest(ExternalRequest r) {
        r.setStatus(RequestStatus.QUEUED);
        queue.offer(r);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void processExternalRequest(ExternalRequest r, String b) {
        Elevator e = strategy.selectElevator(r, elevatorService.getAvailableElevators(b));
        if (e == null) {
            queueExternalRequest(r);
            return;
        }
        requestService.assignRequestToElevator(r.getId(), e.getId());
    }

    public void processPendingRequests(String buildingId) {
        int n = queue.size();
        for (int i = 0; i < n; i++) {
            ExternalRequest r = queue.poll();
            if (r == null) break;
            if (!buildingId.equals(r.getBuildingId())) {
                queue.offer(r);
                continue;
            }
            processExternalRequest(r, buildingId);
        }
    }

    public int drainQueuedForBuilding(String b) {
        int n = 0, size = queue.size();
        for (int i = 0; i < size; i++) {
            ExternalRequest r = queue.poll();
            if (r == null) break;
            if (b.equals(r.getBuildingId())) {
                processExternalRequest(r, b);
                n++;
            } else queue.offer(r);
        }
        return n;
    }
}
