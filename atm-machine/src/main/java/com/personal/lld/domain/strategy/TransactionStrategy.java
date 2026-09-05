package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;

import java.util.Map;

public interface TransactionStrategy {

    Transaction processTransaction(
        String sessionId,
        long amount,
        Map<Denomination, Integer> notes
    );
}
