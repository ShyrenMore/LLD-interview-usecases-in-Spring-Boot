package com.personal.lld.repository;

import com.personal.lld.domain.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class SubscriberRepository {

    private final Map<String, Subscriber> subscribers =
        new ConcurrentHashMap<>();

    public Subscriber save(Subscriber subscriber) {
        subscribers.put(subscriber.getId(), subscriber);
        return subscriber;
    }

    public Optional<Subscriber> findById(String subscriberId) {
        return Optional.ofNullable(subscribers.get(subscriberId));
    }

    public List<Subscriber> findAll() {
        return new ArrayList<>(subscribers.values());
    }

    public void updateOnlineStatus(
        String subscriberId,
        boolean online,
        String connectionId
    ) {
        Subscriber subscriber = subscribers.get(subscriberId);

        if (subscriber != null) {
            subscriber.setOnline(online);
            subscriber.setRealtimeConnectionId(connectionId);
            subscriber.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    public void deleteById(String subscriberId) {
        subscribers.remove(subscriberId);
    }
}
