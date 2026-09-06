package com.personal.lld.repository;

import com.personal.lld.domain.PlaybackSession;

import java.util.*;

public interface PlaybackSessionRepository {
    PlaybackSession save(PlaybackSession session);

    Optional<PlaybackSession> findBySessionId(String sessionId);

    Optional<PlaybackSession> findByUserId(String userId);
}
