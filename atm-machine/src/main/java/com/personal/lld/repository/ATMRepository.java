package com.personal.lld.repository;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.state.ATMState;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ATMRepository {
    private final Map<String, ATM> atmStore = new ConcurrentHashMap<>();

    public ATM save(ATM atm) {
        atmStore.put(atm.getId(), atm);
        return atm;
    }

    public Optional<ATM> findById(String atmId) {
        return Optional.ofNullable(atmStore.get(atmId));
    }

    public List<ATM> findAll() {
        return new ArrayList<>(atmStore.values());
    }

    public void updateATMState(String atmId, ATMState state) {
        findById(atmId).ifPresent(atm -> atm.setCurrentState(state));
    }
}
