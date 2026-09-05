package com.personal.lld.domain.state;

import com.personal.lld.domain.*;
import com.personal.lld.domain.ProcessingPaymentState;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class IdleState implements VendingMachineState {

    @Override
    public Transaction processPayment(VendingMachine machine, PaymentRequest request) {
        log.info(
            "IdleState: Processing payment request for product {}",
            request.getProductId()
        );

        // TODO: Validate product exists and has sufficient stock.
        // TODO: Check if machine has sufficient change.

        Transaction transaction = new Transaction(
            0,
            machine.getId(),
            request.getProductId(),
            request.getTotalAmount()
        );

        machine.setCurrentTransaction(transaction);

        double amountInserted = request.getTotalAmount();
        transaction.addPayment(amountInserted);

        for (Map.Entry<Denomination, Integer> entry : request.getDenominations().entrySet()) {
            machine.addCash(entry.getKey(), entry.getValue());
        }

        machine.setState(new ProcessingPaymentState());

        try {
            Thread.sleep(1000);
            machine.setState(new DispensingState());

            Product product = machine.getInventory()
                .keySet()
                .stream()
                .filter(p -> p.getId() == request.getProductId())
                .findFirst()
                .orElse(null);

            if (product != null) {
                machine.dispenseProduct(product);
                machine.setState(new IdleState());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("IdleState: Payment processing interrupted", e);
            machine.setState(new IdleState());
        }

        log.info(
            "IdleState: Payment processed, transaction created: {}",
            transaction.getId()
        );

        return transaction;
    }

    @Override
    public void cancelPayment(VendingMachine machine, int transactionId) {
        log.info("IdleState: No active transaction to cancel");
    }

    @Override
    public String getStateName() {
        return "IDLE";
    }
}
