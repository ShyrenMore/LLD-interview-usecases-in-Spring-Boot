package com.personal.lld.controller;

import com.personal.lld.domain.Subscriber;
import com.personal.lld.service.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscribers")
@RequiredArgsConstructor
@Slf4j
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping
    public Subscriber registerSubscriber(
        @RequestParam String email
    ) {
        log.info(
            "Registering subscriber with email {}",
            email
        );

        return subscriberService.registerSubscriber(email);
    }

    @PostMapping("/{subscriberId}/online")
    public void goOnline(
        @PathVariable String subscriberId,
        @RequestParam String connectionId
    ) {
        log.info(
            "Subscriber {} going online",
            subscriberId
        );

        subscriberService.goOnline(
            subscriberId,
            connectionId
        );
    }

    @PostMapping("/{subscriberId}/offline")
    public void goOffline(
        @PathVariable String subscriberId
    ) {
        log.info(
            "Subscriber {} going offline",
            subscriberId
        );

        subscriberService.goOffline(subscriberId);
    }
}
