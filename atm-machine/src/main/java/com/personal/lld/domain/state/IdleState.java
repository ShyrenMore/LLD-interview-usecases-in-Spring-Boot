package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.service.CardService;
import com.personal.lld.service.SessionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdleState extends AbstractATMState {

    @Override
    public void insertCard(ATM atm, String cardId) {
        log.info("[IdleState] insertCard: {}", cardId);

        CardService cardService = atm.getCardService();

        if (cardService != null) {
            // TODO: Validate card using CardService.
            log.info("[CardService] validate card and cache details");
        }

        SessionService sessionService = atm.getSessionService();

        if (sessionService != null) {
            log.info("[SessionService] start session");

            atm.setCurrentSession(
                sessionService.startSession(
                    atm.getId(),
                    cardId
                )
            );
        }
    }

    @Override
    public ATMState next(ATM atm) {
        return atm.getCurrentSession() != null
            ? new CardInsertedState()
            : this;
    }
}
