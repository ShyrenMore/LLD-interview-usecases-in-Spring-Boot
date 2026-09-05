package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {

    private String id;
    private String holderName;
    private long balanceMinorUnits;
    private long dailyWithdrawalLimitMinor;
    private long dailyWithdrawalUsedMinor;
    private boolean active;

    public Account(String id, String holderName, long balanceMinorUnits) {
        this.id = id;
        this.holderName = holderName;
        this.balanceMinorUnits = balanceMinorUnits;
        this.dailyWithdrawalLimitMinor = 50000;
        this.dailyWithdrawalUsedMinor = 0;
        this.active = true;
    }
}
