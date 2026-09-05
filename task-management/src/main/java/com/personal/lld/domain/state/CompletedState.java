package com.personal.lld.domain.state;

import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskStatus;

public class CompletedState implements TaskState {

    @Override
    public boolean canTransitionTo(TaskStatus newStatus) {
        // Can transition to IN_PROGRESS (reopen).
        return newStatus == TaskStatus.IN_PROGRESS;
    }

    @Override
    public void performTransition(Task task, TaskStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            task.setStatus(newStatus);
        } else {
            throw new InvalidStateTransitionException(
                "Cannot transition from COMPLETED to " + newStatus
            );
        }
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}
