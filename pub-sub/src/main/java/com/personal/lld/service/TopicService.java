package com.personal.lld.service;

import com.personal.lld.domain.Topic;
import com.personal.lld.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public Topic createTopic(String name) {
        Topic topic = new Topic(
            UUID.randomUUID().toString(),
            name,
            true,
            System.currentTimeMillis()
        );

        return topicRepository.save(topic);
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public void deactivateTopic(String topicId) {
        topicRepository.findById(topicId).ifPresent(topic -> {
            topic.setActive(false);
            topicRepository.save(topic);
        });
    }
}
