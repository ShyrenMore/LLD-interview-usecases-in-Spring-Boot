package com.personal.lld.service.gateway;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentGatewayRouter {

    private final Map<String, PaymentGatewayProvider> providers = new ConcurrentHashMap<>();

    public PaymentGatewayRouter(PaymentGatewayProvider defaultProvider) {
        register(defaultProvider);
    }

    public void register(PaymentGatewayProvider provider) {
        providers.put(provider.getName().toLowerCase(), provider);
    }

    public String selectProvider(String preferredGateway, long amountMinor, String currency) {
        if (preferredGateway != null) {
            String key = preferredGateway.toLowerCase();

            if (providers.containsKey(key)) {
                return key;
            }
        }

        return providers.keySet().stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No payment providers registered"));
    }

    public PaymentGatewayProvider resolve(String gatewayName) {
        PaymentGatewayProvider provider = providers.get(gatewayName.toLowerCase());

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Payment provider not registered: " + gatewayName);
        }

        return provider;
    }
}
