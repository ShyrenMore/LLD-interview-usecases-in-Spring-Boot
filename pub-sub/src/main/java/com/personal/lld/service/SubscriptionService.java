package com.personal.lld.service;

import com.personal.lld.domain.Subscriber;
import com.personal.lld.domain.Subscription;
import com.personal.lld.domain.observer.EmailSubscriber;
import com.personal.lld.domain.observer.RealtimeSubscriber;
import com.personal.lld.repository.SubscriberRepository;
import com.personal.lld.repository.SubscriptionRepository;
import com.personal.lld.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriberRepository subscriberRepository;
    private final TopicRepository topicRepository;

    public Subscription subscribeToTopic(
        String topicId,
        String subscriberId
    ) {
        Subscription subscription = new Subscription(
            UUID.randomUUID().toString(),
            topicId,
            subscriberId,
            true,
            System.currentTimeMillis()
        );

        subscriptionRepository.save(subscription);

        topicRepository.findById(topicId).ifPresent(topic ->
            subscriberRepository.findById(subscriberId).ifPresent(subscriber -> {

                EmailSubscriber emailSubscriber =
                    new EmailSubscriber(subscriber.getEmail());

                topic.getMessageSubject()
                    .addEmailSubscriber(emailSubscriber);

                if (subscriber.isOnline()) {
                    RealtimeSubscriber realtimeSubscriber =
                        new RealtimeSubscriber(
                            subscriber.getRealtimeConnectionId(),
                            subscriberId
                        );

                    topic.getMessageSubject()
                        .addRealtimeSubscriber(realtimeSubscriber);
                }
            })
        );

        return subscription;
    }

    public void unsubscribeFromTopic(
        String topicId,
        String subscriberId
    ) {
        subscriptionRepository.deactivateSubscription(
            topicId,
            subscriberId
        );

        // TODO: Remove subscriber from topic observer lists.

        log.info(
            "Unsubscribed {} from topic {}",
            subscriberId,
            topicId
        );
    }
}
