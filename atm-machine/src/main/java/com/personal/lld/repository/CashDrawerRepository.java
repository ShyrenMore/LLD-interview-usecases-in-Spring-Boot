package com.personal.lld.repository;

import com.personal.lld.domain.CashDrawer;
import com.personal.lld.domain.Denomination;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CashDrawerRepository {
    private final Map<String, CashDrawer> cashDrawerStore = new ConcurrentHashMap<>();

    public CashDrawer save(CashDrawer cashDrawer) {
        cashDrawerStore.put(cashDrawer.getAtmId(), cashDrawer);
        return cashDrawer;
    }

    public Optional<CashDrawer> findByATMId(String atmId) {
        return Optional.ofNullable(cashDrawerStore.get(atmId));
    }

    public void updateCashInventory(String atmId, Map<Denomination, Integer> notes) {
        findByATMId(atmId).ifPresent(drawer ->
                drawer.setNotesByDenomination(new HashMap<>(notes)));
    }
}
