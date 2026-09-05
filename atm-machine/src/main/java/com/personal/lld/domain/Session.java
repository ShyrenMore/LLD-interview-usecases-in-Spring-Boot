package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Session {

    private String id;
    private String atmId;
    private String cardId;
    private String accountId;
    private long startTime;
    private long endTime;
    private boolean active;
    private String currentTransactionId;
    private TransactionType transactionType;
    private long amount;

    public Session(
        String id,
        String atmId,
        String cardId,
        String accountId
    ) {
        this.id = id;
        this.atmId = atmId;
        this.cardId = cardId;
        this.accountId = accountId;
        this.startTime = System.currentTimeMillis();
        this.active = true;
    }

    public void endSession() {
        active = false;
        endTime = System.currentTimeMillis();
    }
}
