package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CashDrawer {

    private String atmId;
    private Map<Denomination, Integer> notesByDenomination;

    public CashDrawer(String atmId) {
        this.atmId = atmId;
        this.notesByDenomination = new HashMap<>();

        for (Denomination denomination : Denomination.values()) {
            notesByDenomination.put(denomination, 0);
        }
    }

    public void addNotes(Denomination denomination, int count) {
        int current = notesByDenomination.getOrDefault(denomination, 0);
        notesByDenomination.put(denomination, current + count);
    }

    public void removeNotes(Denomination denomination, int count) {
        int current = notesByDenomination.getOrDefault(denomination, 0);
        notesByDenomination.put(
            denomination,
            Math.max(0, current - count)
        );
    }

    public int getTotalCash() {
        return notesByDenomination.entrySet()
            .stream()
            .mapToInt(entry ->
                entry.getKey().getValue() * entry.getValue()
            )
            .sum();
    }
}
