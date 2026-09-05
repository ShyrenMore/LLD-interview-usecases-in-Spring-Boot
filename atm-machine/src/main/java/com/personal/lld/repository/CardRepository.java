package com.personal.lld.repository;

import com.personal.lld.domain.Card;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CardRepository {
    private final Map<String, Card> cardStore = new ConcurrentHashMap<>();

    public Card save(Card card) {
        cardStore.put(card.getId(), card);
        return card;
    }

    public Optional<Card> findById(String cardId) {
        return Optional.ofNullable(cardStore.get(cardId));
    }

    public void updatePinRetries(String cardId, int retriesLeft) {
        findById(cardId).ifPresent(card -> card.setPinRetriesLeft(retriesLeft));
    }

    public void blockCard(String cardId) {
        findById(cardId).ifPresent(card -> card.setBlocked(true));
    }
}
