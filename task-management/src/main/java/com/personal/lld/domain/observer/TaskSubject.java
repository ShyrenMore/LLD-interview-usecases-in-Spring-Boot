package com.personal.lld.domain.observer;

import com.personal.lld.domain.ChangeType;

public interface TaskSubject {

    void attach(TaskSubscriber subscriber);

    void detach(TaskSubscriber subscriber);

    void notifySubscribers(
        ChangeType changeType,
        String oldValue,
        String newValue
    );
}
