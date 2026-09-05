package com.personal.lld.repository;

import com.personal.lld.domain.CancellationPolicy;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CancellationPolicyRepository {
    private final Map<String, CancellationPolicy> policies = new ConcurrentHashMap<>();

    public CancellationPolicy save(CancellationPolicy policy) {
        policies.put(policy.getId(), policy);
        return policy;
    }

    public Optional<CancellationPolicy> findById(String policyId) {
        return Optional.ofNullable(policies.get(policyId));
    }
}
