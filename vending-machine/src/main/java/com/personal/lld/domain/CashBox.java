package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CashBox {

    private int id;
    private int vendingMachineId;
    private final Map<Denomination, Integer> denominations;
    private double totalAmount;

    public CashBox(int id, int vendingMachineId) {
        this.id = id;
        this.vendingMachineId = vendingMachineId;
        this.denominations = new HashMap<>();
        this.totalAmount = 0.0;
        initializeCashBox();
    }

    private void initializeCashBox() {
        denominations.put(Denomination.ONE_DOLLAR, 10);
        denominations.put(Denomination.FIVE_DOLLAR, 5);
        denominations.put(Denomination.TEN_DOLLAR, 3);
        denominations.put(Denomination.TWENTY_DOLLAR, 2);
        denominations.put(Denomination.FIFTY_DOLLAR, 1);
        denominations.put(Denomination.HUNDRED_DOLLAR, 1);

        calculateTotalAmount();
    }

    public void addDenomination(Denomination denomination, int count) {
        int currentCount = denominations.getOrDefault(denomination, 0);
        denominations.put(denomination, currentCount + count);
        calculateTotalAmount();
    }

    public boolean removeDenomination(Denomination denomination, int count) {
        int currentCount = denominations.getOrDefault(denomination, 0);

        if (currentCount >= count) {
            denominations.put(denomination, currentCount - count);
            calculateTotalAmount();
            return true;
        }

        return false;
    }

    private void calculateTotalAmount() {
        totalAmount = denominations.entrySet()
            .stream()
            .mapToDouble(entry ->
                entry.getKey().getValueInDollars() * entry.getValue()
            )
            .sum();
    }

    public boolean hasSufficientChange(double amount) {
        return totalAmount >= amount;
    }

    public Map<Denomination, Integer> calculateChange(double amount) {
        Map<Denomination, Integer> change = new HashMap<>();
        double remainingAmount = amount;

        Denomination[] sortedDenominations = {
            Denomination.HUNDRED_DOLLAR,
            Denomination.FIFTY_DOLLAR,
            Denomination.TWENTY_DOLLAR,
            Denomination.TEN_DOLLAR,
            Denomination.FIVE_DOLLAR,
            Denomination.ONE_DOLLAR
        };

        for (Denomination denomination : sortedDenominations) {
            int availableCount = denominations.getOrDefault(denomination, 0);
            double denominationValue = denomination.getValueInDollars();

            if (remainingAmount >= denominationValue && availableCount > 0) {
                int countNeeded = (int) (remainingAmount / denominationValue);
                int countToUse = Math.min(countNeeded, availableCount);

                change.put(denomination, countToUse);
                remainingAmount -= countToUse * denominationValue;

                if (remainingAmount < 0.01) {
                    break;
                }
            }
        }

        return change;
    }

    @Override
    public String toString() {
        return "CashBox " + id + " - Total: $" + totalAmount;
    }
}
