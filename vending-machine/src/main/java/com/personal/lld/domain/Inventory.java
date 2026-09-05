package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Inventory {

    private int productId;
    private int vendingMachineId;
    private int quantity;
    private int minThreshold;

    public Inventory(int productId, int vendingMachineId, int quantity, int minThreshold) {
        this.productId = productId;
        this.vendingMachineId = vendingMachineId;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
    }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    public void addQuantity(int amount) {
        quantity += amount;
    }

    public void removeQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        } else {
            quantity = 0;
        }
    }

    @Override
    public String toString() {
        return "Product " + productId
            + " - Quantity: " + quantity
            + " (Min: " + minThreshold + ")";
    }
}
