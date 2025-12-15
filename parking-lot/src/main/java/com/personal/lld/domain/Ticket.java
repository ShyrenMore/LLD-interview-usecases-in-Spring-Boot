package com.personal.lld.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Ticket {
    private UUID id;
    private UUID vehicleId;
    private UUID slotId;
    private LocalDateTime entryTime;
    private boolean isActive;

    public Ticket(UUID vehicleId, UUID slotId) {
        this.id = UUID.randomUUID();
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = LocalDateTime.now();
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
