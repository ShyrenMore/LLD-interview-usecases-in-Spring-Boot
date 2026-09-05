package com.personal.lld.controller;

import com.personal.lld.domain.Recovery;
import com.personal.lld.domain.RecoveryStatus;
import com.personal.lld.domain.state.VendingMachineState;
import com.personal.lld.service.RecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recovery")
@RequiredArgsConstructor
@Slf4j
public class RecoveryController {

    private final RecoveryService recoveryService;

    @PostMapping("/machines/{machineId}/check")
    public void checkAndRecover(@PathVariable int machineId) {
        log.info(
            "Controller: Checking and performing recovery for machine {}",
            machineId
        );

        recoveryService.performRecovery(machineId);
    }

    @GetMapping("/machines/{machineId}/status")
    public RecoveryStatus getRecoveryStatus(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting recovery status for machine {}",
            machineId
        );

        return recoveryService.getRecoveryStatus(machineId);
    }

    @PostMapping("/machines/{machineId}/recoveries/{recoveryId}/complete")
    public void markRecoveryComplete(
        @PathVariable int machineId,
        @PathVariable int recoveryId
    ) {
        log.info(
            "Controller: Marking recovery {} as complete for machine {}",
            recoveryId,
            machineId
        );

        recoveryService.markRecoveryComplete(machineId, recoveryId);
    }

    @PostMapping("/machines/{machineId}/recoveries")
    public void createRecoveryEntry(
        @PathVariable int machineId,
        @RequestParam int transactionId,
        @RequestBody VendingMachineState state
    ) {
        log.info(
            "Controller: Creating recovery entry for machine {}, transaction {}, state {}",
            machineId,
            transactionId,
            state.getStateName()
        );

        recoveryService.createRecoveryEntry(
            machineId,
            transactionId,
            state
        );
    }

    @GetMapping("/machines/{machineId}/recoveries/pending")
    public List<Recovery> getPendingRecoveries(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting pending recoveries for machine {}",
            machineId
        );

        return recoveryService.getPendingRecoveries(machineId);
    }

    @PostMapping("/startup")
    public void checkAndRecoverAtStartup() {
        log.info(
            "Controller: Checking and recovering during system startup"
        );

        recoveryService.checkAndRecover();
    }
}
