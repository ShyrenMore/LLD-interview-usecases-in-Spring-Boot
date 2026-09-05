package com.personal.lld;

import com.personal.lld.controller.MessageController;
import com.personal.lld.controller.PublisherController;
import com.personal.lld.controller.SubscriberController;
import com.personal.lld.controller.SubscriptionController;
import com.personal.lld.controller.TopicController;
import com.personal.lld.domain.Message;
import com.personal.lld.domain.Subscriber;
import com.personal.lld.domain.Subscription;
import com.personal.lld.domain.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class PubSubSimulation implements CommandLineRunner {

    private final TopicController topicController;
    private final SubscriberController subscriberController;
    private final SubscriptionController subscriptionController;
    private final PublisherController publisherController;
    private final MessageController messageController;

    public static void main(String[] args) {
        SpringApplication.run(PubSubSimulation.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("=== PUB/SUB SYSTEM SIMULATION ===");

        log.info("1. Creating topics...");
        Topic techTopic =
            topicController.createTopic("Technology");
        Topic newsTopic =
            topicController.createTopic("News");

        log.info(
            "Created topics: {}",
            topicController.getAllTopics()
        );

        log.info("2. Registering subscribers...");
        Subscriber alice =
            subscriberController.registerSubscriber(
                "alice@example.com"
            );
        Subscriber bob =
            subscriberController.registerSubscriber(
                "bob@example.com"
            );

        log.info(
            "Registered subscribers: {}, {}",
            alice,
            bob
        );

        log.info("3. Subscribing to topics...");
        Subscription sub1 =
            subscriptionController.subscribeToTopic(
                techTopic.getId(),
                alice.getId()
            );
        Subscription sub2 =
            subscriptionController.subscribeToTopic(
                techTopic.getId(),
                bob.getId()
            );
        Subscription sub3 =
            subscriptionController.subscribeToTopic(
                newsTopic.getId(),
                alice.getId()
            );

        log.info(
            "Subscriptions created: {}, {}, {}",
            sub1,
            sub2,
            sub3
        );

        log.info("4. Publishing messages...");
        Message msg1 =
            publisherController.publishMessage(
                techTopic.getId(),
                "New AI breakthrough announced!"
            );
        Message msg2 =
            publisherController.publishMessage(
                newsTopic.getId(),
                "Breaking: Major political update"
            );

        log.info(
            "Messages published: {}, {}",
            msg1,
            msg2
        );

        log.info("5. Testing online/offline status...");
        subscriberController.goOnline(
            alice.getId(),
            "conn-123"
        );
        subscriberController.goOffline(bob.getId());

        log.info("Status changes applied");

        log.info("6. Publishing another message...");
        Message msg3 =
            publisherController.publishMessage(
                techTopic.getId(),
                "Tech conference next week!"
            );

        log.info(
            "Message published: {}",
            msg3
        );

        log.info("7. Acknowledging message...");
        messageController.acknowledgeMessage(
            msg1.getId(),
            alice.getId()
        );

        log.info("Message acknowledged");
        log.info("=== SIMULATION COMPLETED ===");
    }
}
