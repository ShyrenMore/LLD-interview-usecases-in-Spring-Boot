package com.personal.lld.domain.observer;

import com.personal.lld.domain.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class EmailSubscriber implements SubscriberObserver {

    private final String email;

    public EmailSubscriber(String email) {
        this.email = email;
    }

    @Override
    public void update(Message message) {
        log.info(
            "Sending EMAIL to {}: New message in topic -> {}",
            email,
            message.getContent()
        );
    }
}
