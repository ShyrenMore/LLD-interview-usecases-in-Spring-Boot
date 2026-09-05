package com.personal.lld.domain;

public enum Denomination {

    FIVE_HUNDRED(50000),
    TWO_HUNDRED(20000),
    ONE_HUNDRED(10000);

    private final int value;

    Denomination(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
