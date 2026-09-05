package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PaymentRequest {

    private int productId;
    private int quantity;
    private Map<Denomination, Integer> denominations;

    public PaymentRequest(
        int productId,
        int quantity,
        Map<Denomination, Integer> denominations
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.denominations = denominations;
    }

    public double getTotalAmount() {
        return denominations.entrySet()
            .stream()
            .mapToDouble(entry ->
                entry.getKey().getValueInDollars() * entry.getValue()
            )
            .sum();
    }

    @Override
    public String toString() {
        return "PaymentRequest - Product: " + productId
            + ", Quantity: " + quantity
            + ", Amount: $" + getTotalAmount();
    }
}
