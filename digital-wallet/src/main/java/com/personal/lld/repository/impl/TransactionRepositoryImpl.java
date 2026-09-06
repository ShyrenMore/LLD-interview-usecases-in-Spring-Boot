package com.personal.lld.repository.impl;

import com.personal.lld.domain.Transaction;
import com.personal.lld.repository.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final Map<String, Transaction> transactionsById = new ConcurrentHashMap<>();
    private final Map<String, String> transactionIdByProviderRef = new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactionsById.put(transaction.getId(), transaction);

        if (transaction.getProviderRef() != null) {
            transactionIdByProviderRef.put(transaction.getProviderRef(), transaction.getId());
        }

        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return Optional.ofNullable(transactionsById.get(transactionId));
    }

    @Override
    public Optional<Transaction> findByProviderRef(String providerRef) {
        String id = transactionIdByProviderRef.get(providerRef);

        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(transactionsById.get(id));
    }

    @Override
    public List<Transaction> findByWalletAndRange(String walletId, long startUtc, long endUtc) {
        return transactionsById.values().stream()
                .filter(t -> walletId.equals(t.getFromWalletId())
                        || walletId.equals(t.getToWalletId()))
                .filter(t -> t.getTimestamp() >= startUtc && t.getTimestamp() <= endUtc)
                .sorted(Comparator.comparingLong(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }
}
