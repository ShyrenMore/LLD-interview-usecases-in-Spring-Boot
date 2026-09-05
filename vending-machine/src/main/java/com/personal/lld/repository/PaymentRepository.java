package com.personal.lld.repository;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class PaymentRepository {

    private final Map<Integer, Transaction> transactions = new HashMap<>();
    private final Map<Integer, Map<Denomination, Integer>> cashBoxes = new HashMap<>();
    private int nextTransactionId = 1;

    public PaymentRepository() {
        log.info("PaymentRepository initialized");
    }

    public void saveTransaction(Transaction transaction) {
        if (transaction.getId() == 0) {
            transaction.setId(nextTransactionId++);
        }

        transactions.put(transaction.getId(), transaction);
        log.info("Repository: Saved transaction with ID: {}", transaction.getId());
    }

    public Transaction findById(int transactionId) {
        Transaction transaction = transactions.get(transactionId);

        if (transaction == null) {
            log.warn("Repository: Transaction not found with ID: {}", transactionId);
        } else {
            log.info("Repository: Found transaction with ID: {}", transactionId);
        }

        return transaction;
    }

    public List<Transaction> findByMachine(int machineId) {
        List<Transaction> machineTransactions = transactions.values()
            .stream()
            .filter(transaction -> transaction.getVendingMachineId() == machineId)
            .toList();

        log.info(
            "Repository: Found {} transactions for machine {}",
            machineTransactions.size(),
            machineId
        );

        return machineTransactions;
    }

    public void updateCashBox(
        int machineId,
        Map<Denomination, Integer> denominations
    ) {
        cashBoxes.put(machineId, new HashMap<>(denominations));
        log.info("Repository: Updated cash box for machine {}", machineId);
    }

    public Map<Denomination, Integer> getCashBox(int machineId) {
        Map<Denomination, Integer> cashBox = cashBoxes.computeIfAbsent(
            machineId,
            key -> new HashMap<>()
        );

        return new HashMap<>(cashBox);
    }

    public List<Transaction> getTransactionHistory(int machineId) {
        return findByMachine(machineId);
    }

    public int getTotalTransactions() {
        return transactions.size();
    }

    public double getTotalCashInMachine(int machineId) {
        return getCashBox(machineId)
            .entrySet()
            .stream()
            .mapToDouble(entry ->
                entry.getKey().getValueInDollars() * entry.getValue()
            )
            .sum();
    }
}
