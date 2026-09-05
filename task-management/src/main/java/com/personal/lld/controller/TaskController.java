package com.personal.lld.controller;

import com.personal.lld.domain.Priority;
import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskSearchCriteria;
import com.personal.lld.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Task createTask(
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam(required = false) LocalDateTime dueDate,
        @RequestParam(required = false) String priority,
        @RequestParam int creatorId
    ) {
        // TODO: Validate input parameters.
        // TODO: Convert priority string to Priority enum.

        Task task = new Task(
            0,
            title,
            description,
            dueDate,
            Priority.MEDIUM,
            creatorId
        );

        return taskService.createTask(task);
    }

    @PutMapping("/{taskId}")
    public Task updateTask(
        @PathVariable int taskId,
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam(required = false) LocalDateTime dueDate,
        @RequestParam(required = false) String priority
    ) {
        // TODO: Validate input parameters.
        // TODO: Convert priority string to Priority enum.

        Task updatedTask = new Task(
            taskId,
            title,
            description,
            dueDate,
            Priority.MEDIUM,
            0
        );

        return taskService.updateTask(taskId, updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable int taskId) {
        taskService.deleteTask(taskId);
    }

    @PostMapping("/search")
    public List<Task> searchTasks(
        @RequestBody TaskSearchCriteria criteria
    ) {
        return taskService.searchTasks(criteria);
    }

    @PostMapping("/{parentTaskId}/subtasks")
    public Task addSubtask(
        @PathVariable int parentTaskId,
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam(required = false) LocalDateTime dueDate,
        @RequestParam(required = false) String priority,
        @RequestParam int creatorId
    ) {
        // TODO: Validate input parameters.

        Task subtask = new Task(
            0,
            title,
            description,
            dueDate,
            Priority.MEDIUM,
            creatorId
        );

        return taskService.addSubtask(parentTaskId, subtask);
    }
}
