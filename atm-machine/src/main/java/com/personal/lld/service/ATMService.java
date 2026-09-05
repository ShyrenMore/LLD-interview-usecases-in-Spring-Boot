package com.personal.lld.service;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.CashDrawer;
import com.personal.lld.domain.state.IdleState;
import com.personal.lld.domain.state.OutOfServiceState;
import com.personal.lld.repository.ATMRepository;
import com.personal.lld.repository.CashDrawerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ATMService {
    private final ATMRepository atmRepository;
    private final CashDrawerRepository cashDrawerRepository;

    public void takeOffline(String atmId) {
        atmRepository.findById(atmId).ifPresent(atm -> {
            atm.setOnline(false);
            atm.setCurrentState(new OutOfServiceState());
            atmRepository.save(atm);
        });
    }

    public void bringOnline(String atmId) {
        atmRepository.findById(atmId).ifPresent(atm -> {
            atm.setOnline(true);
            atm.setCurrentState(new IdleState());
            atmRepository.save(atm);
        });
    }

    public CashDrawer auditCash(String atmId) {
        return cashDrawerRepository.findByATMId(atmId).orElse(null);
    }

    public ATM getATM(String atmId) {
        return atmRepository.findById(atmId).orElse(null);
    }
}
