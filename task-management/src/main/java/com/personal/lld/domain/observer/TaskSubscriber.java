package com.personal.lld.domain.observer;

import com.personal.lld.domain.ChangeType;

public interface TaskSubscriber {

    void update(
        int taskId,
        ChangeType changeType,
        String oldValue,
        String newValue
    );
}
