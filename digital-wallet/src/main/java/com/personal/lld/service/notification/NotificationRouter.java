package com.personal.lld.service.notification;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationRouter {

    private final Map<String, NotificationChannel> channels = new ConcurrentHashMap<>();

    public NotificationRouter(
            EmailNotificationChannel emailNotificationChannel,
            SmsNotificationChannel smsNotificationChannel) {

        register("email", emailNotificationChannel);
        register("sms", smsNotificationChannel);
    }

    public void register(String channelName, NotificationChannel channel) {
        channels.put(channelName.toLowerCase(), channel);
    }

    public void send(String channelName, NotificationMessage message) {
        NotificationChannel channel = channels.get(channelName.toLowerCase());

        if (channel == null) {
            throw new IllegalArgumentException(
                    "Notification channel not registered: " + channelName);
        }

        channel.send(message);
    }
}
