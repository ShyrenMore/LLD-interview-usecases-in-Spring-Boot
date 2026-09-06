package com.personal.lld.repository.impl;

import com.personal.lld.domain.PlaybackSession;
import com.personal.lld.repository.PlaybackSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PlaybackSessionRepositoryImpl implements PlaybackSessionRepository {
    private final Map<String, PlaybackSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionByUser = new ConcurrentHashMap<>();

    public PlaybackSession save(PlaybackSession s) {
        sessions.put(s.getSessionId(), s);
        sessionByUser.put(s.getUserId(), s.getSessionId());
        return s;
    }

    public Optional<PlaybackSession> findBySessionId(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public Optional<PlaybackSession> findByUserId(String u) {
        String id = sessionByUser.get(u);
        return id == null ? Optional.empty() : Optional.ofNullable(sessions.get(id));
    }
}
