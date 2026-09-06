package com.personal.lld.service.gateway;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGatewayProvider implements PaymentGatewayProvider {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String initiatePayment(
            String accountNumber,
            long amountMinor,
            String paymentMethod,
            Map<String, String> paymentDetails) {

        return "PG_REF_" + UUID.randomUUID();
    }

    @Override
    public boolean verifyCallback(String providerRef, String status) {
        return true;
    }
}
