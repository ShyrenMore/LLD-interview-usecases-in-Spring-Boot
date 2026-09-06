package com.personal.lld.domain;

import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
public class PlaybackSession {
    private String id, sessionId, userId, currentSongId;
    private long currentPosition;
    private PlaybackSource playbackSource;
    private String sourceId;
    private List<String> queue = new ArrayList<>();
    private boolean shuffleMode;
    private RepeatMode repeatMode;
    private PlaybackStatus status;
    private String deviceId;
    private long startedAt, lastUpdatedAt;

    public PlaybackSession(String id, String sessionId, String userId, String currentSongId, long currentPosition, PlaybackSource playbackSource, String sourceId, List<String> queue, boolean shuffleMode, RepeatMode repeatMode, PlaybackStatus status, String deviceId, long startedAt, long lastUpdatedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.currentSongId = currentSongId;
        this.currentPosition = currentPosition;
        this.playbackSource = playbackSource;
        this.sourceId = sourceId;
        this.queue = queue != null ? new ArrayList<>(queue) : new ArrayList<>();
        this.shuffleMode = shuffleMode;
        this.repeatMode = repeatMode;
        this.status = status;
        this.deviceId = deviceId;
        this.startedAt = startedAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void setQueue(List<String> q) {
        this.queue = q != null ? new ArrayList<>(q) : new ArrayList<>();
    }
}
