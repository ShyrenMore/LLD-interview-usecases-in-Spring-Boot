package com.personal.lld.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListeningHistory {
    private String id, userId, songId;
    private long playedAt, playDuration;
    private boolean completed;
}
