package com.personal.lld.service;

import com.personal.lld.domain.ChangeType;
import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskStatus;
import com.personal.lld.domain.state.CancelledState;
import com.personal.lld.domain.state.CompletedState;
import com.personal.lld.domain.state.InProgressState;
import com.personal.lld.domain.state.ReviewState;
import com.personal.lld.domain.state.TaskState;
import com.personal.lld.domain.state.TodoState;
import com.personal.lld.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@Slf4j
public class TaskStateService {

    private final TaskRepository taskRepository;
    private final Map<TaskStatus, TaskState> stateMap;

    public TaskStateService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.stateMap = initializeStates();
    }

    private Map<TaskStatus, TaskState> initializeStates() {
        Map<TaskStatus, TaskState> states =
            new EnumMap<>(TaskStatus.class);

        states.put(TaskStatus.TODO, new TodoState());
        states.put(TaskStatus.IN_PROGRESS, new InProgressState());
        states.put(TaskStatus.REVIEW, new ReviewState());
        states.put(TaskStatus.COMPLETED, new CompletedState());
        states.put(TaskStatus.CANCELLED, new CancelledState());

        return states;
    }

    public void updateTaskStatus(int taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId);

        if (task == null) {
            throw new RuntimeException("Task not found");
        }

        TaskState currentState = stateMap.get(task.getStatus());

        if (currentState == null) {
            throw new RuntimeException("Invalid current state");
        }

        if (!currentState.canTransitionTo(newStatus)) {
            throw new RuntimeException(
                "Invalid state transition from "
                    + task.getStatus()
                    + " to "
                    + newStatus
            );
        }

        String oldStatus = task.getStatus().toString();

        currentState.performTransition(task, newStatus);
        taskRepository.save(task);

        task.notifySubscribers(
            ChangeType.STATUS_CHANGED,
            oldStatus,
            newStatus.toString()
        );

        log.info(
            "Task {} status changed from {} to {}",
            taskId,
            oldStatus,
            newStatus
        );
    }

    public boolean isValidTransition(
        TaskStatus currentStatus,
        TaskStatus newStatus
    ) {
        TaskState currentState = stateMap.get(currentStatus);

        return currentState != null
            && currentState.canTransitionTo(newStatus);
    }
}
