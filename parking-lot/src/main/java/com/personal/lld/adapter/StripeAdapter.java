package com.personal.lld.adapter;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class StripeAdapter implements PaymentGatewayAdapter {

    @Override
    public boolean pay(UUID ticketId, double amount) {
        // Simulate payment processing
        System.out.println("[ADAPTER] StripeAdapter processing payment for ticket: " + ticketId + " amount: " + amount);

        // Simulate 95% success rate
        Random random = new Random();
        boolean success = random.nextDouble() < 0.95;

        System.out.println("[ADAPTER] StripeAdapter payment result: " + (success ? "SUCCESS" : "FAILED"));

        return success;
    }
}