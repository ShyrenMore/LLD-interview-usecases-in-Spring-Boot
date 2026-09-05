package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;
    private String atmId;
    private String sessionId;
    private String accountId;
    private TransactionType type;
    private long amountMinorUnits;
    private TransactionStatus status;
    private Map<Denomination, Integer> dispensedNotes;
    private Map<Denomination, Integer> depositedNotes;
    private long createdAt;
    private long timeoutAt;

    public Transaction(
        String id,
        String atmId,
        String sessionId,
        String accountId,
        TransactionType type,
        long amountMinorUnits
    ) {
        this.id = id;
        this.atmId = atmId;
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.type = type;
        this.amountMinorUnits = amountMinorUnits;
        this.status = TransactionStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.timeoutAt = createdAt + 300000;
    }
}
