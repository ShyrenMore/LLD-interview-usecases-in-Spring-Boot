package com.personal.lld.service;

import com.personal.lld.domain.ChangeType;
import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskSearchCriteria;
import com.personal.lld.domain.strategy.CreatedDateSortingStrategy;
import com.personal.lld.domain.strategy.DueDateSortingStrategy;
import com.personal.lld.domain.strategy.PrioritySortingStrategy;
import com.personal.lld.domain.strategy.TaskSortingContext;
import com.personal.lld.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(Task task) {
        // TODO: Validate user permissions.
        // TODO: Validate parent task hierarchy if parentTaskId exists.

        Task savedTask = taskRepository.save(task);

        savedTask.notifySubscribers(
            ChangeType.CREATED,
            "",
            savedTask.getTitle()
        );

        return savedTask;
    }

    public Task updateTask(int taskId, Task updatedTask) {
        Task existingTask = taskRepository.findById(taskId);

        if (existingTask == null) {
            throw new RuntimeException("Task not found");
        }

        // TODO: Validate user permissions.
        // TODO: Check state transitions.
        // TODO: Add stale-task / optimistic-lock validation.

        String oldTitle = existingTask.getTitle();

        updatedTask.setId(taskId);
        updatedTask.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(updatedTask);

        savedTask.notifySubscribers(
            ChangeType.UPDATED,
            oldTitle,
            savedTask.getTitle()
        );

        // TODO: Update subtask priorities if needed.
        savedTask.updateSubtaskPriorities();

        return savedTask;
    }

    public void deleteTask(int taskId) {
        Task task = taskRepository.findById(taskId);

        if (task == null) {
            throw new RuntimeException("Task not found");
        }

        // TODO: Validate user permissions.
        // TODO: Handle subtasks (cascade delete or prevent deletion).

        taskRepository.delete(taskId);
    }

    public List<Task> searchTasks(TaskSearchCriteria criteria) {
        List<Task> tasks = taskRepository.search(criteria);

        TaskSortingContext sortingContext = new TaskSortingContext();

        String sortBy = criteria.getSortBy();

        if ("dueDate".equals(sortBy)) {
            sortingContext.setSortingStrategy(
                new DueDateSortingStrategy()
            );
        } else if ("createdDate".equals(sortBy)) {
            sortingContext.setSortingStrategy(
                new CreatedDateSortingStrategy()
            );
        } else {
            sortingContext.setSortingStrategy(
                new PrioritySortingStrategy()
            );
        }

        List<Task> sortedTasks = sortingContext.sortTasks(tasks);

        if ("asc".equalsIgnoreCase(criteria.getSortOrder())) {
            Collections.reverse(sortedTasks);
        }

        return sortedTasks;
    }

    public Task addSubtask(int parentTaskId, Task subtask) {
        Task parentTask = taskRepository.findById(parentTaskId);

        if (parentTask == null) {
            throw new RuntimeException("Parent task not found");
        }

        // TODO: Validate user permissions.
        // TODO: Validate subtask hierarchy.

        subtask.setParentTaskId(parentTaskId);
        subtask.setCreatorId(parentTask.getCreatorId());

        Task savedSubtask = taskRepository.save(subtask);

        savedSubtask.notifySubscribers(
            ChangeType.CREATED,
            "",
            savedSubtask.getTitle()
        );

        return savedSubtask;
    }
}
