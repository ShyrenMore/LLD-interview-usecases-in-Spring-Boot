package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String id;
    private String topicId;
    private String content;
    private long timestamp;

    @Override
    public String toString() {
        return "Message{"
            + "id='" + id + '\''
            + ", topicId='" + topicId + '\''
            + ", content='" + content + '\''
            + ", timestamp=" + timestamp
            + '}';
    }
}
