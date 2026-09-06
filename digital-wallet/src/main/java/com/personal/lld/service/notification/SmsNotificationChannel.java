package com.personal.lld.service.notification;

import org.springframework.stereotype.Component;

@Component("smsNotificationChannel")
public class SmsNotificationChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        System.out.println(
                "[SMS] To: " + message.getTo()
                        + " | Body: " + message.getBody());
    }
}
