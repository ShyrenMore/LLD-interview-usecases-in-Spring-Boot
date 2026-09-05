package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.Denomination;

import java.util.Map;

public interface SupportsNotes {

    void processTransaction(
        ATM atm,
        long amount,
        Map<Denomination, Integer> notes
    );
}
