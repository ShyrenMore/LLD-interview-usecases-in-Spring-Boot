package com.personal.lld.controller;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.Session;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.ATMService;
import com.personal.lld.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {
    private final SessionService sessionService;
    private final ATMService atmService;

    @PostMapping
    public Session startSession(@RequestParam String atmId, @RequestParam String cardId) {
        ATM atm = atmService.getATM(atmId);
        if (atm == null) {
            return null;
        }

        log.info("Session start requested for ATM {} and card {}", atmId, cardId);
        return atm.getCurrentSession();
    }

    @PostMapping("/{sessionId}/end")
    public void endSession(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            return;
        }

        ATM atm = atmService.getATM(session.getAtmId());
        if (atm == null) {
            return;
        }

        try {
            atm.endSession();
        } catch (InvalidATMOperationException ex) {
            log.error("Unable to end session {}: {}", sessionId, ex.getMessage());
        }
    }
}
