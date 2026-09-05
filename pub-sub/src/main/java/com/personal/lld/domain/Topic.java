package com.personal.lld.domain;

import com.personal.lld.domain.observer.MessageSubject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Topic {

    private String id;
    private String name;
    private boolean active;
    private long createdAt;
    private final MessageSubject messageSubject = new MessageSubject();

    public Topic(String id, String name, boolean active, long createdAt) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Topic{"
            + "id='" + id + '\''
            + ", name='" + name + '\''
            + ", active=" + active
            + ", emailSubscribers="
            + messageSubject.getEmailSubscribers().size()
            + ", realtimeSubscribers="
            + messageSubject.getRealtimeSubscribers().size()
            + '}';
    }
}
