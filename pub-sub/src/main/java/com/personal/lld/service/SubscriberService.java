package com.personal.lld.service;

import com.personal.lld.domain.Subscriber;
import com.personal.lld.domain.Subscription;
import com.personal.lld.domain.observer.RealtimeSubscriber;
import com.personal.lld.domain.observer.SubscriberObserver;
import com.personal.lld.repository.MessageDeliveryRepository;
import com.personal.lld.repository.SubscriberRepository;
import com.personal.lld.repository.SubscriptionRepository;
import com.personal.lld.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageDeliveryRepository messageDeliveryRepository;
    private final TopicRepository topicRepository;

    public Subscriber registerSubscriber(String email) {
        long now = System.currentTimeMillis();

        Subscriber subscriber = new Subscriber(
            UUID.randomUUID().toString(),
            email,
            null,
            true,
            now,
            now
        );

        return subscriberRepository.save(subscriber);
    }

    public void goOnline(
        String subscriberId,
        String connectionId
    ) {
        subscriberRepository.updateOnlineStatus(
            subscriberId,
            true,
            connectionId
        );

        List<Subscription> subscriptions =
            subscriptionRepository.findBySubscriber(subscriberId);

        for (Subscription subscription : subscriptions) {
            topicRepository.findById(subscription.getTopicId())
                .ifPresent(topic -> {
                    SubscriberObserver realtimeSubscriber =
                        new RealtimeSubscriber(
                            connectionId,
                            subscriberId
                        );

                    topic.getMessageSubject()
                        .addRealtimeSubscriber(realtimeSubscriber);
                });
        }

        pushPendingDeliveries(subscriberId);
    }

    public void goOffline(String subscriberId) {
        subscriberRepository.updateOnlineStatus(
            subscriberId,
            false,
            null
        );

        List<Subscription> subscriptions =
            subscriptionRepository.findBySubscriber(subscriberId);

        for (Subscription subscription : subscriptions) {
            topicRepository.findById(subscription.getTopicId())
                .ifPresent(topic -> {
                    // TODO: Remove the specific realtime subscriber.
                    topic.getMessageSubject()
                        .removeRealtimeSubscriber(null);
                });
        }
    }

    private void pushPendingDeliveries(String subscriberId) {
        // TODO:
        // 1. Find all topics the subscriber is subscribed to.
        // 2. Find pending messages/deliveries.
        // 3. Send the pending messages.

        log.info(
            "Pushing pending deliveries for subscriber {}",
            subscriberId
        );
    }
}
