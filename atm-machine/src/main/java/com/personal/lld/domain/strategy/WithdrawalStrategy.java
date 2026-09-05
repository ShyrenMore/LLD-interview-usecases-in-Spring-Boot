package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WithdrawalStrategy implements TransactionStrategy {

    @Override
    public Transaction processTransaction(
        String sessionId,
        long amount,
        Map<Denomination, Integer> notes
    ) {
        log.info(
            "[WithdrawalStrategy] Processing withdrawal: {} for session: {}",
            amount,
            sessionId
        );

        Transaction transaction = new Transaction(
            "TXN_" + System.currentTimeMillis(),
            "ATM_001",
            sessionId,
            "ACC_001",
            TransactionType.WITHDRAW,
            amount
        );

        // TODO: Check account balance, daily limits,
        // ATM cash availability and calculate optimal notes.
        // TODO: Update account balance and ATM cash.

        Map<Denomination, Integer> dispensedNotes =
            calculateNotes(amount);

        transaction.setDispensedNotes(dispensedNotes);
        transaction.setStatus(TransactionStatus.SUCCESS);

        log.info(
            "[WithdrawalStrategy] Withdrawal completed, dispensed: {}",
            dispensedNotes
        );

        return transaction;
    }

    private Map<Denomination, Integer> calculateNotes(long amount) {
        Map<Denomination, Integer> notes = new HashMap<>();
        long remaining = amount;

        for (Denomination denomination : Denomination.values()) {
            int count =
                (int) (remaining / denomination.getValue());

            if (count > 0) {
                notes.put(denomination, count);
                remaining -=
                    (long) count * denomination.getValue();
            }
        }

        return notes;
    }
}
