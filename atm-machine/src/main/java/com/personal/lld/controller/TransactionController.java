package com.personal.lld.controller;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Session;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.ATMService;
import com.personal.lld.service.SessionService;
import com.personal.lld.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService; // kept for future direct access if needed
    private final SessionService sessionService;
    private final ATMService atmService;

    @GetMapping("/balance/{sessionId}")
    public Transaction showBalance(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            return null;
        }

        ATM atm = atmService.getATM(session.getAtmId());
        if (atm == null) {
            return null;
        }

        try {
            atm.selectTransaction(TransactionType.BALANCE);
            atm.processTransaction(0);
            return atm.getLastTransaction();
        } catch (InvalidATMOperationException ex) {
            log.error("Balance inquiry failed for session {}: {}", sessionId, ex.getMessage());
            return null;
        }
    }

    @PostMapping("/withdraw/{sessionId}")
    public Transaction withdrawCash(
            @PathVariable String sessionId,
            @RequestParam long amountMinorUnits) {

        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            return null;
        }

        ATM atm = atmService.getATM(session.getAtmId());
        if (atm == null) {
            return null;
        }

        try {
            atm.selectTransaction(TransactionType.WITHDRAW);
            atm.processTransaction(amountMinorUnits);
            return atm.getLastTransaction();
        } catch (InvalidATMOperationException ex) {
            log.error("Withdrawal failed for session {}: {}", sessionId, ex.getMessage());
            return null;
        }
    }

    @PostMapping("/deposit/{sessionId}")
    public Transaction depositCash(
            @PathVariable String sessionId,
            @RequestBody Map<Denomination, Integer> notes) {

        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            return null;
        }

        ATM atm = atmService.getATM(session.getAtmId());
        if (atm == null) {
            return null;
        }

        long amount = calculateAmount(notes);

        try {
            atm.selectTransaction(TransactionType.DEPOSIT);
            atm.processTransaction(amount, notes);
            return atm.getLastTransaction();
        } catch (InvalidATMOperationException ex) {
            log.error("Deposit failed for session {}: {}", sessionId, ex.getMessage());
            return null;
        }
    }

    private long calculateAmount(Map<Denomination, Integer> notes) {
        if (notes == null) {
            return 0;
        }

        return notes.entrySet().stream()
                .mapToLong(entry -> entry.getKey().getValue() * entry.getValue())
                .sum();
    }
}
