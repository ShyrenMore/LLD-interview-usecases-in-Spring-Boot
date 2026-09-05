package com.personal.lld.controller;

import com.personal.lld.domain.Message;
import com.personal.lld.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publish")
@RequiredArgsConstructor
@Slf4j
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping("/topics/{topicId}")
    public Message publishMessage(
        @PathVariable String topicId,
        @RequestParam String content
    ) {
        log.info(
            "Publishing message to topic {}",
            topicId
        );

        return publisherService.publishMessage(
            topicId,
            content
        );
    }
}
