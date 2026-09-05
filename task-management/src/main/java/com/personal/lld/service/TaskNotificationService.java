package com.personal.lld.service;

import com.personal.lld.domain.ChangeType;
import com.personal.lld.domain.TaskChangeLog;
import com.personal.lld.domain.TaskSubscription;
import com.personal.lld.repository.TaskChangeLogRepository;
import com.personal.lld.repository.TaskSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskNotificationService {

    private final TaskSubscriptionRepository taskSubscriptionRepository;
    private final TaskChangeLogRepository taskChangeLogRepository;

    public void subscribeToTask(int taskId, int userId) {
        TaskSubscription subscription =
            new TaskSubscription(0, userId, taskId);

        taskSubscriptionRepository.save(subscription);

        log.info(
            "User {} subscribed to task {}",
            userId,
            taskId
        );
    }

    public void unsubscribeFromTask(int taskId, int userId) {
        // TODO: Implement unsubscribe logic.
        // This would typically mark the subscription as inactive.

        log.info(
            "Unsubscribe requested for user {} from task {}",
            userId,
            taskId
        );
    }

    public void notifySubscribers(
        int taskId,
        ChangeType changeType,
        String oldValue,
        String newValue
    ) {
        // TODO: Implement notification logic.
        // Retrieve active subscribers and notify them.

        log.info(
            "Notification requested for task {}: {} - {} -> {}",
            taskId,
            changeType,
            oldValue,
            newValue
        );
    }

    public List<TaskChangeLog> getTaskHistory(int taskId) {
        return taskChangeLogRepository.findByTaskId(taskId);
    }
}
