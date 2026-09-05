package com.personal.lld.domain.state;

import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskStatus;

public class ReviewState implements TaskState {

    @Override
    public boolean canTransitionTo(TaskStatus newStatus) {
        // Can transition to COMPLETED or IN_PROGRESS.
        return newStatus == TaskStatus.COMPLETED
            || newStatus == TaskStatus.IN_PROGRESS;
    }

    @Override
    public void performTransition(Task task, TaskStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            task.setStatus(newStatus);
        } else {
            throw new InvalidStateTransitionException(
                "Cannot transition from REVIEW to " + newStatus
            );
        }
    }

    @Override
    public String getStateName() {
        return "REVIEW";
    }
}
