package com.personal.lld.controller;

import com.personal.lld.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{messageId}/acknowledge/{subscriberId}")
    public void acknowledgeMessage(
        @PathVariable String messageId,
        @PathVariable String subscriberId
    ) {
        log.info(
            "Acknowledging message {} for subscriber {}",
            messageId,
            subscriberId
        );

        messageService.acknowledgeMessage(messageId, subscriberId);
    }
}
