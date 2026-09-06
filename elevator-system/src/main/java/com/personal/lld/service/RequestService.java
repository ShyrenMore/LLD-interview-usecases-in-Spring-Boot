package com.personal.lld.service;

import java.util.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.personal.lld.domain.*;
import com.personal.lld.repository.*;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final ExternalRequestRepository externalRepo;
    private final InternalRequestRepository internalRepo;

    public ExternalRequest createExternalRequest(int floor, Direction d, String b) {
        return externalRepo.save(new ExternalRequest(b, floor, d));
    }

    public InternalRequest createInternalRequest(String e, int floor) {
        return internalRepo.save(new InternalRequest(e, floor));
    }

    public List<ExternalRequest> getPendingExternalRequests(String b) {
        return externalRepo.findByBuildingAndStatus(b, RequestStatus.PENDING);
    }

    public List<InternalRequest> getPendingRequestsForElevator(String e) {
        return internalRepo.findPendingByElevator(e);
    }

    public List<InternalRequest> getPendingInternalRequestsForElevator(String e) {
        return getPendingRequestsForElevator(e);
    }

    public List<ExternalRequest> getAssignedRequestsForElevator(String e) {
        return externalRepo.findAll().stream().filter(r -> e.equals(r.getAssignedElevatorId()) && r.getStatus() == RequestStatus.ASSIGNED).toList();
    }

    public void assignRequestToElevator(String requestId, String elevatorId) {
        externalRepo.findById(requestId).ifPresent(r -> {
            r.setAssignedElevatorId(elevatorId);
            r.setStatus(RequestStatus.ASSIGNED);
            externalRepo.save(r);
            internalRepo.save(new InternalRequest(elevatorId, r.getFloorNumber()));
        });
    }

    public void completeInternalRequest(String id) {
        internalRepo.findById(id).ifPresent(r -> {
            r.setStatus(RequestStatus.COMPLETED);
            internalRepo.save(r);
        });
    }

    public void completeExternalRequest(String id) {
        externalRepo.findById(id).ifPresent(r -> {
            r.setStatus(RequestStatus.COMPLETED);
            externalRepo.save(r);
        });
    }

    public List<ExternalRequest> getAllExternalRequests() {
        return externalRepo.findAll();
    }
}
