package com.personal.lld.controller;

import com.personal.lld.domain.Subscription;
import com.personal.lld.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/topics/{topicId}/subscribers/{subscriberId}")
    public Subscription subscribeToTopic(
        @PathVariable String topicId,
        @PathVariable String subscriberId
    ) {
        log.info(
            "Subscriber {} subscribing to topic {}",
            subscriberId,
            topicId
        );

        return subscriptionService.subscribeToTopic(
            topicId,
            subscriberId
        );
    }

    @DeleteMapping("/topics/{topicId}/subscribers/{subscriberId}")
    public void unsubscribeFromTopic(
        @PathVariable String topicId,
        @PathVariable String subscriberId
    ) {
        log.info(
            "Subscriber {} unsubscribing from topic {}",
            subscriberId,
            topicId
        );

        subscriptionService.unsubscribeFromTopic(
            topicId,
            subscriberId
        );
    }
}
