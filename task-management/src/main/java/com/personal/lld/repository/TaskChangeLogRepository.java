package com.personal.lld.repository;

import com.personal.lld.domain.TaskChangeLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TaskChangeLogRepository {

    public TaskChangeLog save(TaskChangeLog logEntry) {
        // TODO: Implement actual database save.
        log.info(
            "Saving task change log: {} for task {}",
            logEntry.getChangeType(),
            logEntry.getTaskId()
        );

        return logEntry;
    }

    public List<TaskChangeLog> findByTaskId(int taskId) {
        // TODO: Implement actual database query.
        log.info("Finding change logs for task: {}", taskId);

        return new ArrayList<>();
    }
}
