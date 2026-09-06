package com.personal.lld.repository;

import com.personal.lld.domain.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String transactionId);
    Optional<Transaction> findByProviderRef(String providerRef);
    List<Transaction> findByWalletAndRange(String walletId, long startUtc, long endUtc);
}
