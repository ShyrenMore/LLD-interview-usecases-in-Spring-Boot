package com.personal.lld.repository;

import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {
    private final Map<String, Transaction> transactionStore = new ConcurrentHashMap<>();

    public Transaction save(Transaction transaction) {
        transactionStore.put(transaction.getId(), transaction);
        return transaction;
    }

    public Optional<Transaction> findById(String transactionId) {
        return Optional.ofNullable(transactionStore.get(transactionId));
    }

    public List<Transaction> findBySession(String sessionId) {
        return transactionStore.values().stream()
                .filter(txn -> sessionId.equals(txn.getSessionId()))
                .collect(Collectors.toList());
    }

    public List<Transaction> findByATMAndTimeRange(String atmId, long startTime, long endTime) {
        return transactionStore.values().stream()
                .filter(txn -> atmId.equals(txn.getAtmId()))
                .filter(txn -> txn.getCreatedAt() >= startTime && txn.getCreatedAt() <= endTime)
                .collect(Collectors.toList());
    }

    public void updateTransactionStatus(String transactionId, TransactionStatus status) {
        findById(transactionId).ifPresent(txn -> txn.setStatus(status));
    }
}
