package com.personal.lld.domain.observer;

import com.personal.lld.domain.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageSubject {

    private final List<SubscriberObserver> emailSubscribers =
        new CopyOnWriteArrayList<>();
    private final List<SubscriberObserver> realtimeSubscribers =
        new CopyOnWriteArrayList<>();

    public void addEmailSubscriber(SubscriberObserver subscriber) {
        emailSubscribers.add(subscriber);
    }

    public void removeEmailSubscriber(SubscriberObserver subscriber) {
        emailSubscribers.remove(subscriber);
    }

    public void addRealtimeSubscriber(SubscriberObserver subscriber) {
        realtimeSubscribers.add(subscriber);
    }

    public void removeRealtimeSubscriber(SubscriberObserver subscriber) {
        realtimeSubscribers.remove(subscriber);
    }

    public void notify(Message message) {
        notifyEmailSubscribers(message);
        notifyRealtimeSubscribers(message);
    }

    public void notifyEmailSubscribers(Message message) {
        emailSubscribers.forEach(subscriber -> subscriber.update(message));
    }

    public void notifyRealtimeSubscribers(Message message) {
        realtimeSubscribers.forEach(subscriber -> subscriber.update(message));
    }

    public List<SubscriberObserver> getEmailSubscribers() {
        return new ArrayList<>(emailSubscribers);
    }

    public List<SubscriberObserver> getRealtimeSubscribers() {
        return new ArrayList<>(realtimeSubscribers);
    }
}
