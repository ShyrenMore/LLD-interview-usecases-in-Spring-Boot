package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskSubscription {

    private int id;
    private int userId;
    private int taskId;
    private boolean active;

    public TaskSubscription(
        int id,
        int userId,
        int taskId
    ) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.active = true;
    }
}
