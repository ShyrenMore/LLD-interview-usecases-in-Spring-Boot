package com.personal.lld.repository;

import com.personal.lld.domain.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class SubscriptionRepository {

    private final Map<String, Subscription> subscriptions =
        new ConcurrentHashMap<>();

    public Subscription save(Subscription subscription) {
        subscriptions.put(subscription.getId(), subscription);
        return subscription;
    }

    public List<Subscription> findByTopic(String topicId) {
        return subscriptions.values()
            .stream()
            .filter(subscription ->
                topicId.equals(subscription.getTopicId())
                    && subscription.isActive()
            )
            .toList();
    }

    public List<Subscription> findBySubscriber(String subscriberId) {
        return subscriptions.values()
            .stream()
            .filter(subscription ->
                subscriberId.equals(subscription.getSubscriberId())
                    && subscription.isActive()
            )
            .toList();
    }

    public void deactivateSubscription(
        String topicId,
        String subscriberId
    ) {
        subscriptions.values()
            .stream()
            .filter(subscription ->
                topicId.equals(subscription.getTopicId())
                    && subscriberId.equals(subscription.getSubscriberId())
            )
            .findFirst()
            .ifPresent(subscription ->
                subscription.setActive(false)
            );
    }

    public void deleteById(String subscriptionId) {
        subscriptions.remove(subscriptionId);
    }
}
