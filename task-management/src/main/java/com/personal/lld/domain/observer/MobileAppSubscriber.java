package com.personal.lld.domain.observer;

import com.personal.lld.domain.ChangeType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobileAppSubscriber implements TaskSubscriber {

    private final String pushNotificationService;

    public MobileAppSubscriber(String pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @Override
    public void update(
        int taskId,
        ChangeType changeType,
        String oldValue,
        String newValue
    ) {
        // TODO: Replace pushNotificationService with an actual
        // PushNotificationService dependency.
        // TODO: Find the users subscribed to the task and send
        // the push notification to them.

        log.info(
            "Push notification sent for task {}: {} - {} -> {}",
            taskId,
            changeType,
            oldValue,
            newValue
        );
    }
}
