package com.personal.lld.controller;

import com.personal.lld.domain.TaskChangeLog;
import com.personal.lld.service.TaskNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskNotificationController {

    private final TaskNotificationService taskNotificationService;

    @PostMapping("/{taskId}/subscriptions/{userId}")
    public void subscribeToTask(
        @PathVariable int taskId,
        @PathVariable int userId
    ) {
        log.info(
            "User {} subscribing to task {}",
            userId,
            taskId
        );

        taskNotificationService.subscribeToTask(taskId, userId);
    }

    @DeleteMapping("/{taskId}/subscriptions/{userId}")
    public void unsubscribeFromTask(
        @PathVariable int taskId,
        @PathVariable int userId
    ) {
        log.info(
            "User {} unsubscribing from task {}",
            userId,
            taskId
        );

        taskNotificationService.unsubscribeFromTask(taskId, userId);
    }

    @GetMapping("/{taskId}/history")
    public List<TaskChangeLog> getTaskHistory(
        @PathVariable int taskId
    ) {
        return taskNotificationService.getTaskHistory(taskId);
    }
}
