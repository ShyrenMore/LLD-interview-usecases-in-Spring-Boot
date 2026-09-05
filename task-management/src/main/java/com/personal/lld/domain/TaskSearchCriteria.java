package com.personal.lld.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class TaskSearchCriteria {

    private Integer assigneeId;
    private Integer creatorId;
    private Priority priority;
    private TaskStatus status;
    private DateRange dueDateRange;
    private List<String> tags;
    private Boolean hasSubtasks;
    private String sortBy;
    private String sortOrder;

    public TaskSearchCriteria() {
        this.sortBy = "priority";
        this.sortOrder = "desc";
    }

    public TaskSearchCriteria assigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
        return this;
    }

    public TaskSearchCriteria creatorId(Integer creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public TaskSearchCriteria priority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public TaskSearchCriteria status(TaskStatus status) {
        this.status = status;
        return this;
    }

    public TaskSearchCriteria dueDateRange(DateRange dueDateRange) {
        this.dueDateRange = dueDateRange;
        return this;
    }

    public TaskSearchCriteria tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public TaskSearchCriteria hasSubtasks(Boolean hasSubtasks) {
        this.hasSubtasks = hasSubtasks;
        return this;
    }

    public TaskSearchCriteria sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public TaskSearchCriteria sortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
}
