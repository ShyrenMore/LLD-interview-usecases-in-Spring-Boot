package com.personal.lld.service.notification;

import com.personal.lld.domain.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void send(NotificationMessage message) {
        log.info(
                "Notification sent. to={}, subject={}, body={}",
                message.getTo(),
                message.getSubject(),
                message.getBody());
    }
}
