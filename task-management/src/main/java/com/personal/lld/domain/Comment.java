package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {

    private int id;
    private int taskId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;

    public Comment(
        int id,
        int taskId,
        int userId,
        String content
    ) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
