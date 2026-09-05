package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.exception.InvalidATMOperationException;

public abstract class AbstractATMState implements ATMState {

    @Override
    public void insertCard(ATM atm, String cardId) {
        throw operationNotAllowed();
    }

    @Override
    public void ejectCard(ATM atm) {
        throw operationNotAllowed();
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        throw operationNotAllowed();
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type) {
        throw operationNotAllowed();
    }

    @Override
    public void processTransaction(ATM atm, long amount) {
        throw operationNotAllowed();
    }

    @Override
    public void endSession(ATM atm) {
        throw operationNotAllowed();
    }

    @Override
    public ATMState next(ATM atm) {
        return this;
    }

    private InvalidATMOperationException operationNotAllowed() {
        return new InvalidATMOperationException(
            "Operation not allowed in " + getClass().getSimpleName()
        );
    }
}
