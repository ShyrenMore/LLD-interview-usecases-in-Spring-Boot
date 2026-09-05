package com.personal.lld.domain.state;

import com.personal.lld.domain.PaymentRequest;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.VendingMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("dispensingState")
public class DispensingState implements VendingMachineState {

    @Override
    public Transaction processPayment(VendingMachine machine, PaymentRequest request) {
        log.warn("DispensingState: Cannot process new payment while dispensing");
        // Cannot process new payment while dispensing
        return null;
    }

    @Override
    public void cancelPayment(VendingMachine machine, int transactionId) {
        log.warn("DispensingState: Cannot cancel payment while dispensing");
        // Cannot cancel payment while dispensing
    }

    @Override
    public String getStateName() {
        return "DISPENSING";
    }
}