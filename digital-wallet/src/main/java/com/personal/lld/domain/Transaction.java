package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id;
    private String fromWalletId;
    private String toWalletId;
    private long amountMinor;
    private TransactionType type;
    private TransactionStatus status;
    private String providerRef;
    private String description;
    private long timestamp;
}
