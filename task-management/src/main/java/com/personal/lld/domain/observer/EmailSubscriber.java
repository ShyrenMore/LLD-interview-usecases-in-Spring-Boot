package com.personal.lld.domain.observer;

import com.personal.lld.domain.ChangeType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailSubscriber implements TaskSubscriber {

    private final String emailService;

    public EmailSubscriber(String emailService) {
        this.emailService = emailService;
    }

    @Override
    public void update(
        int taskId,
        ChangeType changeType,
        String oldValue,
        String newValue
    ) {
        // TODO: Replace emailService with an actual EmailService dependency.
        // TODO: Retrieve subscribed email addresses from the repository
        // and send the notification to all subscribers.

        log.info(
            "Email notification sent for task {}: {} - {} -> {}",
            taskId,
            changeType,
            oldValue,
            newValue
        );
    }
}
