package com.personal.lld.domain;

import com.personal.lld.domain.state.ATMState;
import com.personal.lld.domain.state.IdleState;
import com.personal.lld.domain.state.SupportsNotes;
import com.personal.lld.service.CardService;
import com.personal.lld.service.SessionService;
import com.personal.lld.service.TransactionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Setter
@Slf4j
public class ATM {

    private String id;
    private String location;
    private boolean online;
    private ATMState currentState;
    private CashDrawer cashDrawer;
    private Session currentSession;
    private CardService cardService;
    private SessionService sessionService;
    private TransactionService transactionService;
    private Transaction lastTransaction;

    public ATM(String id, String location) {
        this.id = id;
        this.location = location;
        this.online = true;
        this.currentState = new IdleState();
        this.cashDrawer = new CashDrawer(id);
    }

    public void attachServices(
        CardService cardService,
        SessionService sessionService,
        TransactionService transactionService
    ) {
        this.cardService = cardService;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
    }

    public void insertCard(String cardId) {
        currentState.insertCard(this, cardId);
        autoNext();
    }

    public void ejectCard() {
        currentState.ejectCard(this);
        autoNext();
    }

    public void enterPin(String pin) {
        currentState.enterPin(this, pin);
        autoNext();
    }

    public void selectTransaction(TransactionType type) {
        currentState.selectTransaction(this, type);
        autoNext();
    }

    public void processTransaction(long amount) {
        currentState.processTransaction(this, amount);
        autoNext();
    }

    public void processTransaction(
        long amount,
        Map<Denomination, Integer> notes
    ) {
        if (currentState instanceof SupportsNotes supportsNotes) {
            supportsNotes.processTransaction(this, amount, notes);
        } else {
            currentState.processTransaction(this, amount);
        }
        autoNext();
    }

    public void endSession() {
        currentState.endSession(this);
        autoNext();
    }

    private void autoNext() {
        ATMState nextState = currentState.next(this);

        if (nextState != null && nextState != currentState) {
            log.info(
                "[STATE] {} -> {}",
                currentState.getClass().getSimpleName(),
                nextState.getClass().getSimpleName()
            );
            currentState = nextState;
        }
    }
}
