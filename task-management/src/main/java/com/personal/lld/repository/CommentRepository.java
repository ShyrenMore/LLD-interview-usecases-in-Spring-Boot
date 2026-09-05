package com.personal.lld.repository;

import com.personal.lld.domain.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class CommentRepository {

    private final Map<Integer, Comment> comments = new HashMap<>();
    private int nextId = 1;

    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            comment.setId(nextId++);
        }

        comments.put(comment.getId(), comment);

        log.info(
                "Saved comment {} for task {}",
                comment.getId(),
                comment.getTaskId()
        );

        return comment;
    }

    public List<Comment> findByTaskId(int taskId) {
        List<Comment> taskComments = comments.values()
                .stream()
                .filter(comment -> comment.getTaskId() == taskId)
                .toList();

        log.info(
                "Found {} comments for task {}",
                taskComments.size(),
                taskId
        );

        return taskComments;
    }
}
