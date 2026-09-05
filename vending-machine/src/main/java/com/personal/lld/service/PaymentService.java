package com.personal.lld.service;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.PaymentRequest;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.VendingMachine;
import com.personal.lld.repository.PaymentRepository;
import com.personal.lld.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final VendingMachineRepository vendingMachineRepository;
    private final PaymentRepository paymentRepository;

    public Transaction processPayment(
        int machineId,
        PaymentRequest request
    ) {
        log.info(
            "PaymentService: Processing payment for machine {}, product {}",
            machineId,
            request.getProductId()
        );

        VendingMachine machine =
            vendingMachineRepository.findById(machineId);

        if (machine == null) {
            log.warn("PaymentService: Machine not found");
            return null;
        }

        Transaction transaction = machine.processPayment(request);

        if (transaction != null) {
            paymentRepository.saveTransaction(transaction);

            log.info(
                "PaymentService: Payment processed successfully, transaction ID: {}",
                transaction.getId()
            );
        } else {
            log.warn("PaymentService: Payment processing failed");
        }

        return transaction;
    }

    public void cancelPayment(int machineId, int transactionId) {
        log.info(
            "PaymentService: Cancelling payment for machine {}, transaction {}",
            machineId,
            transactionId
        );

        VendingMachine machine =
            vendingMachineRepository.findById(machineId);

        if (machine != null) {
            machine.cancelPayment(transactionId);

            Transaction transaction =
                paymentRepository.findById(transactionId);

            if (transaction != null) {
                transaction.cancel();
                paymentRepository.saveTransaction(transaction);
            }

            log.info("PaymentService: Payment cancelled successfully");
        } else {
            log.warn(
                "PaymentService: Machine not found for payment cancellation"
            );
        }
    }

    public String getPaymentStatus(
        int machineId,
        int transactionId
    ) {
        log.info(
            "PaymentService: Getting payment status for machine {}, transaction {}",
            machineId,
            transactionId
        );

        Transaction transaction =
            paymentRepository.findById(transactionId);

        return transaction != null
            ? transaction.getStatus().toString()
            : "NOT_FOUND";
    }

    public void updateCashBox(
        int machineId,
        Map<Denomination, Integer> denominations
    ) {
        log.info(
            "PaymentService: Updating cash box for machine {}",
            machineId
        );

        paymentRepository.updateCashBox(machineId, denominations);

        log.info("PaymentService: Cash box updated successfully");
    }

    public List<Transaction> getTransactionHistory(int machineId) {
        log.info(
            "PaymentService: Getting transaction history for machine {}",
            machineId
        );

        List<Transaction> transactions =
            paymentRepository.findByMachine(machineId);

        log.info(
            "PaymentService: Retrieved {} transactions",
            transactions.size()
        );

        return transactions;
    }

    public double getTotalCashInMachine(int machineId) {
        log.info(
            "PaymentService: Getting total cash in machine {}",
            machineId
        );

        double totalCash =
            paymentRepository.getTotalCashInMachine(machineId);

        log.info(
            "PaymentService: Total cash in machine: ${}",
            totalCash
        );

        return totalCash;
    }

    public Map<Denomination, Integer> getCashBoxStatus(int machineId) {
        log.info(
            "PaymentService: Getting cash box status for machine {}",
            machineId
        );

        return paymentRepository.getCashBox(machineId);
    }
}
