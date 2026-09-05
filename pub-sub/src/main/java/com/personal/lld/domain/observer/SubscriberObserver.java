package com.personal.lld.domain.observer;

import com.personal.lld.domain.Message;

public interface SubscriberObserver {

    void update(Message message);
}
