package com.personal.lld.service.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String to;
    private String subject;
    private String body;
}
