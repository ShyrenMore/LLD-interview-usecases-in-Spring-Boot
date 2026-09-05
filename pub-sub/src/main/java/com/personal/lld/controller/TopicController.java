package com.personal.lld.controller;

import com.personal.lld.domain.Topic;
import com.personal.lld.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    public Topic createTopic(
        @RequestParam String name
    ) {
        log.info("Creating topic: {}", name);

        return topicService.createTopic(name);
    }

    @GetMapping
    public List<Topic> getAllTopics() {
        log.info("Retrieving all topics");

        return topicService.getAllTopics();
    }

    @PatchMapping("/{topicId}/deactivate")
    public void deactivateTopic(
        @PathVariable String topicId
    ) {
        log.info(
            "Deactivating topic {}",
            topicId
        );

        topicService.deactivateTopic(topicId);
    }
}
