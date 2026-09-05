package com.personal.lld.domain.state;

import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskStatus;

public class CancelledState implements TaskState {

    @Override
    public boolean canTransitionTo(TaskStatus newStatus) {
        // Can transition to TODO (reactivate).
        return newStatus == TaskStatus.TODO;
    }

    @Override
    public void performTransition(Task task, TaskStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            task.setStatus(newStatus);
        } else {
            throw new InvalidStateTransitionException(
                "Cannot transition from CANCELLED to " + newStatus
            );
        }
    }

    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
