package com.personal.lld.repository;

import com.personal.lld.domain.Recovery;
import com.personal.lld.domain.RecoveryStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class RecoveryRepository {

    private final Map<Integer, Recovery> recoveries = new HashMap<>();
    private int nextRecoveryId = 1;

    public RecoveryRepository() {
        log.info("RecoveryRepository initialized");
    }

    public void saveRecovery(Recovery recovery) {
        if (recovery.getId() == 0) {
            recovery.setId(nextRecoveryId++);
        }

        recoveries.put(recovery.getId(), recovery);
        log.info("Repository: Saved recovery with ID: {}", recovery.getId());
    }

    public Recovery findById(int recoveryId) {
        Recovery recovery = recoveries.get(recoveryId);

        if (recovery == null) {
            log.warn("Repository: Recovery not found with ID: {}", recoveryId);
        } else {
            log.info("Repository: Found recovery with ID: {}", recoveryId);
        }

        return recovery;
    }

    public List<Recovery> findPendingRecoveries(int machineId) {
        List<Recovery> pendingRecoveries = recoveries.values()
            .stream()
            .filter(recovery ->
                recovery.getVendingMachineId() == machineId
                    && recovery.getStatus() == RecoveryStatus.PENDING
            )
            .toList();

        log.info(
            "Repository: Found {} pending recoveries for machine {}",
            pendingRecoveries.size(),
            machineId
        );

        return pendingRecoveries;
    }

    public void markComplete(int recoveryId) {
        Recovery recovery = findById(recoveryId);

        if (recovery != null) {
            recovery.markComplete();
            log.info("Repository: Marked recovery {} as complete", recoveryId);
        }
    }

    public List<Recovery> findAll() {
        return new ArrayList<>(recoveries.values());
    }

    public int getTotalRecoveries() {
        return recoveries.size();
    }
}
