package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Task;

import java.util.List;

public interface TaskSortingStrategy {

    List<Task> sort(List<Task> tasks);

    String getStrategyName();
}
