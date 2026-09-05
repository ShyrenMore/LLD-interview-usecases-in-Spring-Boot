package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class BalanceInquiryStrategy implements TransactionStrategy {

    @Override
    public Transaction processTransaction(
        String sessionId,
        long amount,
        Map<Denomination, Integer> notes
    ) {
        log.info(
            "[BalanceInquiryStrategy] Processing balance inquiry for session: {}",
            sessionId
        );

        Transaction transaction = new Transaction(
            "TXN_" + System.currentTimeMillis(),
            "ATM_001",
            sessionId,
            "ACC_001",
            TransactionType.BALANCE,
            0
        );

        // TODO: Get and set actual balance from the bank/account service.
        transaction.setStatus(TransactionStatus.SUCCESS);

        log.info(
            "[BalanceInquiryStrategy] Balance inquiry completed"
        );

        return transaction;
    }
}
