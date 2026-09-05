package com.personal.lld.controller;

import com.personal.lld.domain.TaskStatus;
import com.personal.lld.service.TaskStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskStateController {

    private final TaskStateService taskStateService;

    @PatchMapping("/{taskId}/status")
    public void updateTaskStatus(
        @PathVariable int taskId,
        @RequestParam TaskStatus newStatus
    ) {
        log.info(
            "Updating task {} status to {}",
            taskId,
            newStatus
        );

        taskStateService.updateTaskStatus(taskId, newStatus);
    }
}
