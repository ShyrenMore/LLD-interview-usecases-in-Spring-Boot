package com.personal.lld.domain;

import com.personal.lld.domain.state.VendingMachineState;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Recovery {

    private int id;
    private final int vendingMachineId;
    private final int transactionId;
    private final VendingMachineState state;
    private RecoveryStatus status;
    private final long createdAt;
    private Long completedAt;

    public Recovery(
        int id,
        int vendingMachineId,
        int transactionId,
        VendingMachineState state
    ) {
        this.id = id;
        this.vendingMachineId = vendingMachineId;
        this.transactionId = transactionId;
        this.state = state;
        this.status = RecoveryStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(RecoveryStatus status) {
        this.status = status;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public void markComplete() {
        this.status = RecoveryStatus.COMPLETED;
        this.completedAt = System.currentTimeMillis();
    }

    public boolean isPending() {
        return status == RecoveryStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == RecoveryStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Recovery " + id
            + " - Machine: " + vendingMachineId
            + ", Transaction: " + transactionId
            + ", Status: " + status;
    }
}
