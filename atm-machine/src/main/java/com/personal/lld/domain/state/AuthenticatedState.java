package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.SessionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticatedState extends AbstractATMState {

    @Override
    public void insertCard(ATM atm, String cardId) {
        throw new InvalidATMOperationException("Card already inserted");
    }

    @Override
    public void ejectCard(ATM atm) {
        log.info("[AuthenticatedState] ejectCard");
        endSession(atm);
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        throw new InvalidATMOperationException("Already authenticated");
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type) {
        log.info(
            "[AuthenticatedState] selectTransaction: {}",
            type
        );

        if (atm.getCurrentSession() != null) {
            atm.getCurrentSession().setTransactionType(type);
        }
    }

    @Override
    public void endSession(ATM atm) {
        log.info("[AuthenticatedState] endSession");

        SessionService sessionService = atm.getSessionService();

        if (sessionService != null && atm.getCurrentSession() != null) {
            sessionService.endSession(
                atm.getCurrentSession().getId()
            );
            atm.setCurrentSession(null);
        }
    }

    @Override
    public ATMState next(ATM atm) {
        if (atm.getCurrentSession() == null) {
            return new IdleState();
        }

        if (atm.getCurrentSession().getTransactionType() != null) {
            return new TransactionSelectedState();
        }

        return this;
    }
}
