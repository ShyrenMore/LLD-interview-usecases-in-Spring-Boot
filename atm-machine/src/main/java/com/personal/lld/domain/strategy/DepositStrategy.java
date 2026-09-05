package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DepositStrategy implements TransactionStrategy {

    @Override
    public Transaction processTransaction(
        String sessionId,
        long amount,
        Map<Denomination, Integer> notes
    ) {
        log.info(
            "[DepositStrategy] Processing deposit: {} for session: {}",
            amount,
            sessionId
        );

        Transaction transaction = new Transaction(
            "TXN_" + System.currentTimeMillis(),
            "ATM_001",
            sessionId,
            "ACC_001",
            TransactionType.DEPOSIT,
            amount
        );

        // TODO: Validate deposited notes.
        // TODO: Update account balance and ATM cash inventory.

        if (notes != null) {
            transaction.setDepositedNotes(notes);
        }

        transaction.setStatus(TransactionStatus.SUCCESS);

        log.info(
            "[DepositStrategy] Deposit completed, deposited: {}",
            notes
        );

        return transaction;
    }
}
