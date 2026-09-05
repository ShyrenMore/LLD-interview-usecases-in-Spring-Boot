package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {

    private String id;
    private String accountId;
    private String expiry;
    private boolean blocked;
    private int pinRetriesLeft;

    public Card(String id, String accountId, String expiry) {
        this.id = id;
        this.accountId = accountId;
        this.expiry = expiry;
        this.blocked = false;
        this.pinRetriesLeft = 3;
    }

    public void decrementPinRetries() {
        pinRetriesLeft--;
        if (pinRetriesLeft <= 0) {
            blocked = true;
        }
    }

    public void resetPinRetries() {
        pinRetriesLeft = 3;
    }
}
