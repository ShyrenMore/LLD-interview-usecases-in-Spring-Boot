package com.personal.lld.service;

import com.personal.lld.domain.ChangeType;
import com.personal.lld.domain.Task;
import com.personal.lld.repository.TaskRepository;
import com.personal.lld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void assignTask(int taskId, int assigneeId) {
        if (userRepository.findById(assigneeId) == null) {
            throw new IllegalArgumentException(
                "Assignee with ID " + assigneeId + " not found"
            );
        }

        Task task = taskRepository.findById(taskId);

        if (task == null) {
            throw new IllegalArgumentException(
                "Task with ID " + taskId + " not found"
            );
        }

        int oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(assigneeId);

        taskRepository.save(task);

        task.notifySubscribers(
            ChangeType.ASSIGNED,
            String.valueOf(oldAssigneeId),
            String.valueOf(assigneeId)
        );

        log.info(
            "Task {} assigned from user {} to user {}",
            taskId,
            oldAssigneeId,
            assigneeId
        );
    }
}
