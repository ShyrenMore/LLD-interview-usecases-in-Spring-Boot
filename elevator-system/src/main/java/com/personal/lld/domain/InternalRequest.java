package com.personal.lld.domain;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalRequest {
    private final String id = UUID.randomUUID().toString();
    private final String elevatorId;
    private final int destinationFloor;
    private final long timestamp = System.currentTimeMillis();
    private RequestStatus status = RequestStatus.PENDING;

    public InternalRequest(String elevatorId, int destinationFloor) {
        this.elevatorId = elevatorId;
        this.destinationFloor = destinationFloor;
    }
}
