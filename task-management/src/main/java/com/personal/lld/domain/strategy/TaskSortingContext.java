package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Task;

import java.util.List;

public class TaskSortingContext {

    private TaskSortingStrategy strategy;

    public void setSortingStrategy(TaskSortingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Task> sortTasks(List<Task> tasks) {
        if (strategy == null) {
            strategy = new PrioritySortingStrategy();
        }

        return strategy.sort(tasks);
    }
}
