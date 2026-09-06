package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationMessage {
    private final String to;
    private final String subject;
    private final String body;
}
