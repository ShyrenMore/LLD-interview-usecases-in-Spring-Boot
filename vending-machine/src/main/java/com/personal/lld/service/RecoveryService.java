package com.personal.lld.service;

import com.personal.lld.domain.Recovery;
import com.personal.lld.domain.RecoveryStatus;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.VendingMachine;
import com.personal.lld.domain.state.IdleState;
import com.personal.lld.domain.state.VendingMachineState;
import com.personal.lld.repository.PaymentRepository;
import com.personal.lld.repository.RecoveryRepository;
import com.personal.lld.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecoveryService {

    private final VendingMachineRepository vendingMachineRepository;
    private final RecoveryRepository recoveryRepository;
    private final PaymentRepository paymentRepository;

    public void performRecovery(int machineId) {
        VendingMachine machine = vendingMachineRepository.findById(machineId);

        if (machine == null) {
            log.warn("RecoveryService: Invalid machine ID: {}", machineId);
            return;
        }

        log.info(
            "RecoveryService: Performing recovery for machine {}",
            machineId
        );

        List<Recovery> pendingRecoveries =
            recoveryRepository.findPendingRecoveries(machineId);

        if (!pendingRecoveries.isEmpty()) {
            log.info(
                "RecoveryService: Found {} pending recoveries",
                pendingRecoveries.size()
            );

            for (Recovery recovery : pendingRecoveries) {
                processRecovery(recovery);
            }

            machine.setState(new IdleState());
            log.info(
                "RecoveryService: Machine {} reset to IDLE state",
                machineId
            );
            return;
        }

        log.info(
            "RecoveryService: No pending recoveries for machine {}",
            machineId
        );

        String currentState = machine.getCurrentStateName();

        if (!"IDLE".equals(currentState)) {
            log.info(
                "RecoveryService: Machine {} found in {} state, creating recovery",
                machineId,
                currentState
            );

            List<Transaction> incompleteTransactions =
                findIncompleteTransactions(machineId);

            if (!incompleteTransactions.isEmpty()) {
                Transaction latestTransaction =
                    incompleteTransactions.get(incompleteTransactions.size() - 1);

                createRecoveryEntry(
                    machineId,
                    latestTransaction.getId(),
                    machine.getCurrentState()
                );

                List<Recovery> newRecoveries =
                    recoveryRepository.findPendingRecoveries(machineId);

                for (Recovery recovery : newRecoveries) {
                    if (recovery.getTransactionId() == latestTransaction.getId()) {
                        performRecovery(machineId);
                        break;
                    }
                }
            } else {
                log.info(
                    "RecoveryService: No incomplete transactions found, resetting machine state to IDLE"
                );
            }

            machine.setState(new IdleState());
            log.info(
                "RecoveryService: Machine {} reset to IDLE state",
                machineId
            );
        }

        log.info(
            "RecoveryService: Recovery completed for machine {}",
            machineId
        );
    }

    private void processRecovery(Recovery recovery) {
        log.info(
            "RecoveryService: Processing recovery {}",
            recovery.getId()
        );

        int transactionId = recovery.getTransactionId();
        VendingMachineState state = recovery.getState();

        Transaction transaction = paymentRepository.findById(transactionId);

        if (transaction == null) {
            log.warn(
                "RecoveryService: Transaction not found for recovery {}",
                recovery.getId()
            );
            recovery.markComplete();
            recoveryRepository.saveRecovery(recovery);
            return;
        }

        String stateName = state.getStateName();

        if ("PROCESSING_PAYMENT".equals(stateName)) {
            handleProcessingPaymentRecovery(recovery, transaction);
        } else if ("DISPENSING".equals(stateName)) {
            handleDispensingRecovery(recovery, transaction);
        } else {
            log.warn(
                "RecoveryService: Unknown state for recovery {}: {}",
                recovery.getId(),
                stateName
            );
            recovery.markComplete();
        }

        recoveryRepository.saveRecovery(recovery);

        log.info(
            "RecoveryService: Recovery {} processed successfully",
            recovery.getId()
        );
    }

    private void handleProcessingPaymentRecovery(
        Recovery recovery,
        Transaction transaction
    ) {
        log.info(
            "RecoveryService: Handling PROCESSING_PAYMENT recovery for transaction {}",
            transaction.getId()
        );

        // TODO: Implement actual refund logic.
        // For now, just cancel the transaction.
        transaction.cancel();
        paymentRepository.saveTransaction(transaction);

        log.info(
            "RecoveryService: Refunded payment for transaction {}",
            transaction.getId()
        );
    }

    private void handleDispensingRecovery(
        Recovery recovery,
        Transaction transaction
    ) {
        log.info(
            "RecoveryService: Handling DISPENSING recovery for transaction {}",
            transaction.getId()
        );

        // TODO: Implement actual dispensing logic.
        // For now, just mark the transaction as completed.
        transaction.setStatus(TransactionStatus.COMPLETED);
        paymentRepository.saveTransaction(transaction);

        log.info(
            "RecoveryService: Completed dispensing for transaction {}",
            transaction.getId()
        );
    }

    public RecoveryStatus getRecoveryStatus(int machineId) {
        log.info(
            "RecoveryService: Getting recovery status for machine {}",
            machineId
        );

        List<Recovery> pendingRecoveries =
            recoveryRepository.findPendingRecoveries(machineId);

        return pendingRecoveries.isEmpty()
            ? RecoveryStatus.COMPLETED
            : RecoveryStatus.PENDING;
    }

    public void createRecoveryEntry(
        int machineId,
        int transactionId,
        VendingMachineState state
    ) {
        log.info(
            "RecoveryService: Creating recovery entry for machine {}, transaction {}, state {}",
            machineId,
            transactionId,
            state.getStateName()
        );

        Recovery recovery = new Recovery(
            0,
            machineId,
            transactionId,
            state
        );

        recoveryRepository.saveRecovery(recovery);

        log.info(
            "RecoveryService: Recovery entry created with ID {}",
            recovery.getId()
        );
    }

    public void markRecoveryComplete(int machineId, int recoveryId) {
        log.info(
            "RecoveryService: Marking recovery {} as complete for machine {}",
            recoveryId,
            machineId
        );

        recoveryRepository.markComplete(recoveryId);

        log.info(
            "RecoveryService: Recovery {} marked as complete",
            recoveryId
        );
    }

    public List<Recovery> getPendingRecoveries(int machineId) {
        log.info(
            "RecoveryService: Getting pending recoveries for machine {}",
            machineId
        );

        return recoveryRepository.findPendingRecoveries(machineId);
    }

    /**
     * Check and recover during system startup.
     */
    public void checkAndRecover() {
        log.info(
            "RecoveryService: Checking and recovering during system startup"
        );

        List<VendingMachine> machines = vendingMachineRepository.findAll();

        for (VendingMachine machine : machines) {
            String currentState = machine.getCurrentStateName();

            if (!"IDLE".equals(currentState)) {
                log.info(
                    "RecoveryService: Machine {} found in {} state during startup",
                    machine.getId(),
                    currentState
                );

                List<Transaction> incompleteTransactions =
                    findIncompleteTransactions(machine.getId());

                if (!incompleteTransactions.isEmpty()) {
                    Transaction latestTransaction =
                        incompleteTransactions.get(incompleteTransactions.size() - 1);

                    createRecoveryEntry(
                        machine.getId(),
                        latestTransaction.getId(),
                        machine.getCurrentState()
                    );

                    List<Recovery> newRecoveries =
                        recoveryRepository.findPendingRecoveries(machine.getId());

                    for (Recovery recovery : newRecoveries) {
                        if (recovery.getTransactionId() == latestTransaction.getId()) {
                            processRecovery(recovery);
                            break;
                        }
                    }
                } else {
                    // No incomplete transactions, create recovery with transaction ID 0.
                    createRecoveryEntry(
                        machine.getId(),
                        0,
                        machine.getCurrentState()
                    );
                }

                machine.setState(new IdleState());

                log.info(
                    "RecoveryService: Machine {} reset to IDLE state",
                    machine.getId()
                );
            }
        }

        log.info("RecoveryService: System startup recovery completed");
    }

    private List<Transaction> findIncompleteTransactions(int machineId) {
        List<Transaction> allTransactions =
            paymentRepository.findByMachine(machineId);

        List<Transaction> incompleteTransactions = new ArrayList<>();

        for (Transaction transaction : allTransactions) {
            if (transaction.getStatus() == TransactionStatus.PENDING) {
                incompleteTransactions.add(transaction);
            }
        }

        log.info(
            "RecoveryService: Found {} incomplete transactions for machine {}",
            incompleteTransactions.size(),
            machineId
        );

        return incompleteTransactions;
    }
}
