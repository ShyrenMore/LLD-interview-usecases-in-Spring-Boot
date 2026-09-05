package com.personal.lld.repository;

import com.personal.lld.domain.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class TopicRepository {

    private final Map<String, Topic> topics =
        new ConcurrentHashMap<>();

    public Topic save(Topic topic) {
        topics.put(topic.getId(), topic);
        return topic;
    }

    public List<Topic> findAll() {
        return new ArrayList<>(topics.values());
    }

    public Optional<Topic> findById(String topicId) {
        return Optional.ofNullable(topics.get(topicId));
    }

    public void deleteById(String topicId) {
        topics.remove(topicId);
    }
}
