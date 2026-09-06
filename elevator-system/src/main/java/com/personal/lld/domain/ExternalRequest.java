package com.personal.lld.domain;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalRequest {
    private final String id = UUID.randomUUID().toString();
    private final String buildingId;
    private final int floorNumber;
    private final Direction direction;
    private final long timestamp = System.currentTimeMillis();
    private RequestStatus status = RequestStatus.QUEUED;
    private String assignedElevatorId;

    public ExternalRequest(String buildingId, int floorNumber, Direction direction) {
        this.buildingId = buildingId;
        this.floorNumber = floorNumber;
        this.direction = direction;
    }
}
