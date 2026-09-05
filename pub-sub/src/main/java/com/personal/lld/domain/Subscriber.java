package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    private String id;
    private String email;
    private String realtimeConnectionId;
    private boolean online;
    private long createdAt;
    private long lastHeartbeat;

    @Override
    public String toString() {
        return "Subscriber{"
            + "id='" + id + '\''
            + ", email='" + email + '\''
            + ", online=" + online
            + '}';
    }
}
