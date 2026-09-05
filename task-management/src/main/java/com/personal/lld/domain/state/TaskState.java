package com.personal.lld.domain.state;

import com.personal.lld.domain.Task;
import com.personal.lld.domain.TaskStatus;

public interface TaskState {

    boolean canTransitionTo(TaskStatus newStatus);

    void performTransition(Task task, TaskStatus newStatus);

    String getStateName();
}
