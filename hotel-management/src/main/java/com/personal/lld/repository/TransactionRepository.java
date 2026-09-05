package com.personal.lld.repository;

import com.personal.lld.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TransactionRepository {
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, String> providerRefToId = new ConcurrentHashMap<>();

    public Transaction save(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        if (transaction.getProviderRef() != null && !transaction.getProviderRef().isBlank()) {
            providerRefToId.put(transaction.getProviderRef(), transaction.getId());
        }
        return transaction;
    }

    public Optional<Transaction> findById(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    public Optional<Transaction> findByProviderRef(String providerRef) {
        String transactionId = providerRefToId.get(providerRef);
        return transactionId == null
                ? Optional.empty()
                : Optional.ofNullable(transactions.get(transactionId));
    }
}
