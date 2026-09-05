package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    private String id;
    private String topicId;
    private String subscriberId;
    private boolean active;
    private long createdAt;

    @Override
    public String toString() {
        return "Subscription{"
            + "id='" + id + '\''
            + ", topicId='" + topicId + '\''
            + ", subscriberId='" + subscriberId + '\''
            + ", active=" + active
            + '}';
    }
}
