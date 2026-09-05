package com.personal.lld.domain;

import com.personal.lld.domain.state.IdleState;
import com.personal.lld.domain.state.VendingMachineState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessingPaymentState implements VendingMachineState {

    @Override
    public Transaction processPayment(VendingMachine machine, PaymentRequest request) {
        log.warn("ProcessingPaymentState: Cannot process new payment while already processing");
        return null;
    }

    @Override
    public void cancelPayment(VendingMachine machine, int transactionId) {
        log.info("ProcessingPaymentState: Cancelling payment for transaction {}", transactionId);

        Transaction transaction = machine.getCurrentTransaction();

        if (transaction != null && transaction.getId() == transactionId) {
            double amountToRefund = transaction.getAmountInserted();

            // TODO: Implement actual refund logic

            transaction.cancel();
            machine.setCurrentTransaction(null);
            machine.setState(new IdleState());

            log.info(
                "ProcessingPaymentState: Payment cancelled, refunded ${}",
                amountToRefund
            );
        } else {
            log.warn("ProcessingPaymentState: Transaction not found for cancellation");
        }
    }

    @Override
    public String getStateName() {
        return "PROCESSING_PAYMENT";
    }
}
