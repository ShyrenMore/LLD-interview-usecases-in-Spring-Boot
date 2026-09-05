package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskChangeLog {

    private int id;
    private int taskId;
    private int userId;
    private ChangeType changeType;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;

    public TaskChangeLog(
        int id,
        int taskId,
        int userId,
        ChangeType changeType,
        String oldValue,
        String newValue
    ) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }
}
