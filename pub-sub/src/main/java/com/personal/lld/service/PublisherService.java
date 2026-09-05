package com.personal.lld.service;

import com.personal.lld.domain.Message;
import com.personal.lld.domain.Topic;
import com.personal.lld.repository.MessageDeliveryRepository;
import com.personal.lld.repository.MessageRepository;
import com.personal.lld.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublisherService {

    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;
    private final MessageDeliveryRepository messageDeliveryRepository;

    public Message publishMessage(
        String topicId,
        String content
    ) {
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(
                () -> new RuntimeException(
                    "Topic not found: " + topicId
                )
            );

        if (!topic.isActive()) {
            throw new RuntimeException(
                "Topic is inactive: " + topicId
            );
        }

        Message message = new Message(
            UUID.randomUUID().toString(),
            topicId,
            content,
            System.currentTimeMillis()
        );

        messageRepository.save(message);

        CompletableFuture.runAsync(
            () -> processMessageDeliveryAsync(message, topic)
        );

        return message;
    }

    private void processMessageDeliveryAsync(
        Message message,
        Topic topic
    ) {
        topic.getMessageSubject().notify(message);

        // TODO: Create delivery records for tracking realtime deliveries.

        log.info(
            "Background processing completed for message {}",
            message.getId()
        );
    }
}
