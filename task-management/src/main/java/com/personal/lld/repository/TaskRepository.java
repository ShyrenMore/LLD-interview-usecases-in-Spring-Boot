package com.personal.lld.repository;

import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class TaskRepository {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private int nextId = 1;

    public Task save(Task task) {
        if (task.getId() == 0) {
            task.setId(nextId++);
        }

        tasks.put(task.getId(), task);
        log.info("Saved task with ID: {}", task.getId());

        return task;
    }

    public Task findById(int taskId) {
        return tasks.get(taskId);
    }

    public List<Task> findByAssignee(int assigneeId) {
        return tasks.values()
            .stream()
            .filter(task -> task.getAssigneeId() == assigneeId)
            .toList();
    }

    public List<Task> findByParentTask(int parentTaskId) {
        return tasks.values()
            .stream()
            .filter(task ->
                task.getParentTaskId() != null
                    && task.getParentTaskId() == parentTaskId
            )
            .toList();
    }

    public List<Task> search(TaskSearchCriteria criteria) {
        // TODO: Implement actual search logic.
        return List.copyOf(tasks.values());
    }

    public void delete(int taskId) {
        tasks.remove(taskId);
        log.info("Deleted task with ID: {}", taskId);
    }
}
