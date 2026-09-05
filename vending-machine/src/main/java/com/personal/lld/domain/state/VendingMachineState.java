package com.personal.lld.domain.state;


import com.personal.lld.domain.PaymentRequest;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.VendingMachine;

public interface VendingMachineState {
    Transaction processPayment(VendingMachine machine, PaymentRequest request);
    void cancelPayment(VendingMachine machine, int transactionId);
    String getStateName();
}
