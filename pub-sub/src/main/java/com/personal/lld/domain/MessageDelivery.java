package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDelivery {

    private String id;
    private String messageId;
    private String subscriberId;
    private DeliveryChannel channel;
    private DeliveryStatus status;
    private long createdAt;
    private Long acknowledgedAt;

    @Override
    public String toString() {
        return "MessageDelivery{"
            + "id='" + id + '\''
            + ", messageId='" + messageId + '\''
            + ", subscriberId='" + subscriberId + '\''
            + ", channel=" + channel
            + ", status=" + status
            + '}';
    }
}
