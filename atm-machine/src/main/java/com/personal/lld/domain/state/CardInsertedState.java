package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.CardService;
import com.personal.lld.service.SessionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardInsertedState extends AbstractATMState {

    @Override
    public void insertCard(ATM atm, String cardId) {
        throw new InvalidATMOperationException("Card already inserted");
    }

    @Override
    public void ejectCard(ATM atm) {
        log.info("[CardInsertedState] ejectCard");

        SessionService sessionService = atm.getSessionService();

        if (sessionService != null && atm.getCurrentSession() != null) {
            sessionService.endSession(
                atm.getCurrentSession().getId()
            );
            atm.setCurrentSession(null);
        }
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        log.info("[CardInsertedState] enterPin");

        CardService cardService = atm.getCardService();
        boolean authenticated = true;

        if (cardService != null && atm.getCurrentSession() != null) {
            authenticated = cardService.authenticateCard(
                atm.getCurrentSession().getCardId(),
                pin
            );
        }

        if (!authenticated) {
            throw new InvalidATMOperationException(
                "Authentication failed"
            );
        }
    }

    @Override
    public void endSession(ATM atm) {
        log.info("[CardInsertedState] endSession");

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

        return new AuthenticatedState();
    }
}
