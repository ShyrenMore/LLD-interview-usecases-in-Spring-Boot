package com.personal.lld.domain.state;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.exception.InvalidATMOperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutOfServiceState extends AbstractATMState {

    @Override
    public void insertCard(ATM atm, String cardId) {
        throw new InvalidATMOperationException(
            "ATM is out of service"
        );
    }

    @Override
    public void ejectCard(ATM atm) {
        log.info("[OutOfServiceState] ejectCard (if any)");
    }
}
