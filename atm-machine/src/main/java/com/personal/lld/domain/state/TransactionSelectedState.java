package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.TransactionService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

@Slf4j
public class TransactionSelectedState
    extends AbstractATMState
    implements SupportsNotes {

    @Override
    public void insertCard(ATM atm, String cardId) {
        throw new InvalidATMOperationException("Card already inserted");
    }

    @Override
    public void ejectCard(ATM atm) {
        log.info("[TransactionSelectedState] ejectCard");
        atm.setCurrentState(new IdleState());
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        throw new InvalidATMOperationException("Already authenticated");
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type) {
        log.info(
            "[TransactionSelectedState] update transaction: {}",
            type
        );

        if (atm.getCurrentSession() != null) {
            atm.getCurrentSession().setTransactionType(type);
        }
    }

    @Override
    public void processTransaction(
        ATM atm,
        long amount
    ) {
        TransactionService transactionService =
            atm.getTransactionService();

        TransactionType type =
            atm.getCurrentSession() != null
                ? atm.getCurrentSession().getTransactionType()
                : null;

        if (transactionService == null || type == null) {
            throw new InvalidATMOperationException(
                "Transaction not initialized"
            );
        }

        log.info(
            "[TransactionSelectedState] process: type={}, amount={}",
            type,
            amount
        );

        String sessionId = atm.getCurrentSession().getId();

        Transaction transaction;

        switch (type) {
            case BALANCE:
                transaction =
                    transactionService.showBalance(sessionId);
                break;
            case WITHDRAW:
                transaction =
                    transactionService.withdrawCash(
                        sessionId,
                        amount
                    );
                break;
            case DEPOSIT:
                transaction =
                    transactionService.depositCash(
                        sessionId,
                        Collections.emptyMap()
                    );
                break;
            default:
                throw new InvalidATMOperationException(
                    "Unsupported transaction type"
                );
        }

        atm.setLastTransaction(transaction);
    }

    @Override
    public void processTransaction(
        ATM atm,
        long amount,
        Map<Denomination, Integer> notes
    ) {
        TransactionService transactionService =
            atm.getTransactionService();

        TransactionType type =
            atm.getCurrentSession() != null
                ? atm.getCurrentSession().getTransactionType()
                : null;

        if (transactionService == null || type == null) {
            throw new InvalidATMOperationException(
                "Transaction not initialized"
            );
        }

        log.info(
            "[TransactionSelectedState] process with notes: type={}, amount={}",
            type,
            amount
        );

        if (type == TransactionType.DEPOSIT) {
            Transaction transaction =
                transactionService.depositCash(
                    atm.getCurrentSession().getId(),
                    notes
                );

            atm.setLastTransaction(transaction);
            return;
        }

        processTransaction(atm, amount);
    }

    @Override
    public void endSession(ATM atm) {
        log.info("[TransactionSelectedState] endSession");
        atm.setCurrentState(new IdleState());
    }

    @Override
    public ATMState next(ATM atm) {
        if (atm.getLastTransaction() != null
            && atm.getLastTransaction().getStatus()
                == TransactionStatus.SUCCESS) {
            return new TransactionCompletedState();
        }

        if (atm.getCurrentSession() == null) {
            return new IdleState();
        }

        return this;
    }
}
