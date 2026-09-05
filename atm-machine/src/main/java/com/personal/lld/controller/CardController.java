package com.personal.lld.controller;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import com.personal.lld.service.ATMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atms/{atmId}/card")
@RequiredArgsConstructor
@Slf4j
public class CardController {
    private final ATMService atmService;

    @PostMapping("/insert")
    public boolean insertCard(@PathVariable String atmId, @RequestParam String cardId) {
        ATM atm = atmService.getATM(atmId);
        if (atm == null) {
            return false;
        }

        try {
            atm.insertCard(cardId);
            return true;
        } catch (InvalidATMOperationException ex) {
            log.error("Unable to insert card for ATM {}: {}", atmId, ex.getMessage());
            return false;
        }
    }

    @PostMapping("/eject")
    public void ejectCard(@PathVariable String atmId) {
        ATM atm = atmService.getATM(atmId);
        if (atm == null) {
            return;
        }

        try {
            atm.ejectCard();
        } catch (InvalidATMOperationException ex) {
            log.error("Unable to eject card for ATM {}: {}", atmId, ex.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public boolean authenticateCard(
            @PathVariable String atmId,
            @RequestParam String cardId,
            @RequestParam String pin) {

        ATM atm = atmService.getATM(atmId);
        if (atm == null) {
            return false;
        }

        try {
            atm.enterPin(pin);
            return true;
        } catch (InvalidATMOperationException ex) {
            log.error("Authentication failed for ATM {}, card {}: {}", atmId, cardId, ex.getMessage());
            return false;
        }
    }
}
