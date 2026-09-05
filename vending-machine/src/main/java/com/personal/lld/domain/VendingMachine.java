package com.personal.lld.domain;

import com.personal.lld.domain.state.IdleState;
import com.personal.lld.domain.state.VendingMachineState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class VendingMachine {

    private int id;
    private final String location;
    private VendingMachineState currentState;
    private Transaction currentTransaction;
    private final Map<Product, Integer> inventory;
    private final CashBox cashBox;
    private boolean operational;

    public VendingMachine(int id, String location) {
        this.id = id;
        this.location = location;
        this.currentState = new IdleState();
        this.inventory = new HashMap<>();
        this.cashBox = new CashBox(1, id);
        this.operational = true;

        log.info("VendingMachine created: {} (ID: {})", location, id);
    }

    public void setState(VendingMachineState newState) {
        this.currentState = newState;
        log.info(
            "Machine {} state changed to: {}",
            id,
            newState.getStateName()
        );
    }

    public Transaction processPayment(PaymentRequest request) {
        return currentState.processPayment(this, request);
    }

    public void cancelPayment(int transactionId) {
        currentState.cancelPayment(this, transactionId);
    }

    public String getCurrentStateName() {
        return currentState.getStateName();
    }

    public void addProduct(Product product, int quantity) {
        inventory.merge(product, quantity, Integer::sum);

        log.info(
            "Added {} units of {} to machine {}",
            quantity,
            product.getName(),
            id
        );
    }

    public boolean hasProduct(Product product) {
        return inventory.getOrDefault(product, 0) > 0;
    }

    public void dispenseProduct(Product product) {
        if (hasProduct(product)) {
            inventory.computeIfPresent(
                product,
                (key, quantity) -> quantity - 1
            );

            log.info(
                "Dispensed {} from machine {}",
                product.getName(),
                id
            );
        } else {
            log.warn(
                "Cannot dispense {} - out of stock in machine {}",
                product.getName(),
                id
            );
        }
    }

    public void addCash(Denomination denomination, int count) {
        cashBox.addDenomination(denomination, count);

        log.info(
            "Added {} {} notes to machine {}",
            count,
            denomination,
            id
        );
    }

    public boolean removeCash(Denomination denomination, int count) {
        boolean success = cashBox.removeDenomination(denomination, count);

        if (success) {
            log.info(
                "Removed {} {} notes from machine {}",
                count,
                denomination,
                id
            );
        } else {
            log.warn(
                "Failed to remove {} {} notes from machine {}",
                count,
                denomination,
                id
            );
        }

        return success;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCurrentTransaction(Transaction transaction) {
        this.currentTransaction = transaction;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    @Override
    public String toString() {
        return "VendingMachine{"
            + "id=" + id
            + ", location='" + location + '\''
            + ", state=" + currentState.getStateName()
            + ", operational=" + operational
            + '}';
    }
}
