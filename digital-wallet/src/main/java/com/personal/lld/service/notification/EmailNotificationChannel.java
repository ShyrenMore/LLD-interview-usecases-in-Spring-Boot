package com.personal.lld.service.notification;

import org.springframework.stereotype.Component;

@Component("emailNotificationChannel")
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        System.out.println(
                "[EMAIL] To: " + message.getTo()
                        + " | Subject: " + message.getSubject()
                        + " | Body: " + message.getBody());
    }
}
