package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatement {
    private String walletId;
    private String walletAccountNumber;
    private List<Transaction> transactions;
    private Long startDateUtc;
    private Long endDateUtc;
    private long currentBalanceMinor;
}
