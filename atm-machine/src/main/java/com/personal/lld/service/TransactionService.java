package com.personal.lld.service;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.strategy.BalanceInquiryStrategy;
import com.personal.lld.domain.strategy.DepositStrategy;
import com.personal.lld.domain.strategy.TransactionStrategy;
import com.personal.lld.domain.strategy.WithdrawalStrategy;
import com.personal.lld.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final Map<TransactionType, TransactionStrategy> strategies;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.strategies = new EnumMap<>(TransactionType.class);
        strategies.put(TransactionType.WITHDRAW, new WithdrawalStrategy());
        strategies.put(TransactionType.DEPOSIT, new DepositStrategy());
        strategies.put(TransactionType.BALANCE, new BalanceInquiryStrategy());
    }

    public Transaction showBalance(String sessionId) {
        return processTransaction(sessionId, 0, null, TransactionType.BALANCE);
    }

    public Transaction withdrawCash(String sessionId, long amountMinorUnits) {
        return processTransaction(sessionId, amountMinorUnits, null, TransactionType.WITHDRAW);
    }

    public Transaction depositCash(String sessionId, Map<Denomination, Integer> notes) {
        long amount = calculateAmount(notes);
        return processTransaction(sessionId, amount, notes, TransactionType.DEPOSIT);
    }

    public void acknowledgeTransaction(String transactionId) {
        transactionRepository.updateTransactionStatus(transactionId, TransactionStatus.SUCCESS);
    }

    public boolean validateTransaction(Transaction transaction) {
        // TODO: Implement transaction validation logic
        return true;
    }

    private Transaction processTransaction(
            String sessionId,
            long amount,
            Map<Denomination, Integer> notes,
            TransactionType type) {

        TransactionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        Transaction transaction = strategy.processTransaction(sessionId, amount, notes);
        return transactionRepository.save(transaction);
    }

    private long calculateAmount(Map<Denomination, Integer> notes) {
        if (notes == null) {
            return 0;
        }

        return notes.entrySet().stream()
                .mapToLong(entry -> entry.getKey().getValue() * entry.getValue())
                .sum();
    }
}
