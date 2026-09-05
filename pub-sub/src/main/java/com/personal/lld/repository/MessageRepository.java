package com.personal.lld.repository;

import com.personal.lld.domain.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class MessageRepository {

    private final Map<String, Message> messages =
        new ConcurrentHashMap<>();

    public Message save(Message message) {
        messages.put(message.getId(), message);
        return message;
    }

    public Optional<Message> findById(String messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    public void deleteById(String messageId) {
        messages.remove(messageId);
    }
}
