package com.personal.lld.domain.observer;

import com.personal.lld.domain.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class RealtimeSubscriber implements SubscriberObserver {

    private String connectionId;
    private String subscriberId;

    public RealtimeSubscriber(String connectionId, String subscriberId) {
        this.connectionId = connectionId;
        this.subscriberId = subscriberId;
    }

    @Override
    public void update(Message message) {
        log.info(
            "Sending REALTIME to {} (connection: {}): New message -> {}",
            subscriberId,
            connectionId,
            message.getContent()
        );
    }
}
