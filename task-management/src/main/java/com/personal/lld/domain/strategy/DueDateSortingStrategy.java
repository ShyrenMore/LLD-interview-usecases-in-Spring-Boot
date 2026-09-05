package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DueDateSortingStrategy implements TaskSortingStrategy {

    @Override
    public List<Task> sort(List<Task> tasks) {
        List<Task> mutableTasks = new ArrayList<>(tasks);

        mutableTasks.sort(
            Comparator.comparing(
                Task::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
        );

        return mutableTasks;
    }

    @Override
    public String getStrategyName() {
        return "DUE_DATE";
    }
}
