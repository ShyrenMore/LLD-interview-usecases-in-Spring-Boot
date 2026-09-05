package com.personal.lld.domain;

import com.personal.lld.domain.observer.EmailSubscriber;
import com.personal.lld.domain.observer.TaskSubscriber;
import com.personal.lld.domain.observer.TaskSubject;
import com.personal.lld.domain.state.TaskState;
import com.personal.lld.domain.state.TodoState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Task implements TaskSubject {

    private int id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Priority priority;
    private TaskStatus status;
    private int assigneeId;
    private int creatorId;
    private Integer parentTaskId;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<TaskSubscriber> subscribers;

    private TaskState currentState;

    public Task(
        int id,
        String title,
        String description,
        LocalDateTime dueDate,
        Priority priority,
        int creatorId
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.creatorId = creatorId;
        this.status = TaskStatus.TODO;
        this.tags = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.subscribers = new ArrayList<>();
        this.currentState = new TodoState();

        subscribers.add(new EmailSubscriber("emailService"));
    }

    // Observer Pattern
    @Override
    public void attach(TaskSubscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    @Override
    public void detach(TaskSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void notifySubscribers(
        ChangeType changeType,
        String oldValue,
        String newValue
    ) {
        for (TaskSubscriber subscriber : subscribers) {
            subscriber.update(
                id,
                changeType,
                oldValue,
                newValue
            );
        }
    }

    // State Pattern
    public void setState(TaskState newState) {
        this.currentState = newState;
    }

    // Recursive Subtask Methods
    public List<Task> getSubtasks() {
        // TODO: Implement database query to get immediate subtasks.
        return new ArrayList<>();
    }

    public List<Task> getAllSubtasks() {
        // TODO: Implement recursive query to get all nested subtasks.
        return new ArrayList<>();
    }

    public boolean hasSubtasks() {
        // TODO: Implement check for subtasks.
        return false;
    }

    public int getSubtaskCount() {
        // TODO: Implement count of subtasks.
        return 0;
    }

    public void updateSubtaskPriorities() {
        // TODO: Implement recursive priority update logic.
    }
}
