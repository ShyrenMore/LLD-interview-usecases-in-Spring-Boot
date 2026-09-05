package com.personal.lld.repository;

import com.personal.lld.domain.TaskSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TaskSubscriptionRepository {

    public TaskSubscription save(TaskSubscription subscription) {
        // TODO: Implement actual database save.
        log.info(
            "Saving subscription: User {} -> Task {}",
            subscription.getUserId(),
            subscription.getTaskId()
        );

        return subscription;
    }

    public List<TaskSubscription> findByTaskId(int taskId) {
        // TODO: Implement actual database query.
        log.info("Finding subscriptions for task: {}", taskId);

        return new ArrayList<>();
    }

    public List<TaskSubscription> findByUserId(int userId) {
        // TODO: Implement actual database query.
        log.info("Finding subscriptions for user: {}", userId);

        return new ArrayList<>();
    }
}
