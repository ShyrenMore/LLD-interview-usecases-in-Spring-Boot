package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    private String id;
    private String accountNumber;
    private long balanceMinor;
    private String userId;
    private WalletStatus status;
    private long createdAt;
    private long updatedAt;
}
