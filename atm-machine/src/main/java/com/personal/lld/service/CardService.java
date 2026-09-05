package com.personal.lld.service;

import com.personal.lld.domain.Card;
import com.personal.lld.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;

    public boolean validateCard(String cardId) {
        // TODO: Validate card with bank server and save it in repo, use caching here for some minutes
        Card card = cardRepository.findById(cardId).orElse(null);
        return card != null && !card.isBlocked();
    }

    public void ejectCard(String atmId) {
        // TODO: Delete from cache, or where the ATM is saving
        log.info("Card ejected from ATM: {}", atmId);
    }

    public boolean authenticateCard(String cardId, String pin) {
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card == null || card.isBlocked()) {
            return false;
        }

        // TODO: Validate PIN with bank server
        boolean isValidPin = true;

        if (isValidPin) {
            card.resetPinRetries();
        } else {
            card.decrementPinRetries();
        }

        cardRepository.save(card);
        return isValidPin;
    }
}
