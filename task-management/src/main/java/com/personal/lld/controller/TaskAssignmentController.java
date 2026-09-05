package com.personal.lld.controller;

import com.personal.lld.service.TaskAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    @PostMapping("/{taskId}/assignee/{assigneeId}")
    public void assignTask(
        @PathVariable int taskId,
        @PathVariable int assigneeId
    ) {
        log.info(
            "Assigning task {} to user {}",
            taskId,
            assigneeId
        );

        taskAssignmentService.assignTask(taskId, assigneeId);
    }
}
