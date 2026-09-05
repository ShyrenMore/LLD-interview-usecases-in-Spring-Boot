package com.personal.lld.repository;

import com.personal.lld.domain.Session;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SessionRepository {
    private final Map<String, Session> sessionStore = new ConcurrentHashMap<>();

    public Session save(Session session) {
        sessionStore.put(session.getId(), session);
        return session;
    }

    public Optional<Session> findById(String sessionId) {
        return Optional.ofNullable(sessionStore.get(sessionId));
    }

    public Optional<Session> findActiveByATM(String atmId) {
        return sessionStore.values().stream()
                .filter(session -> atmId.equals(session.getAtmId()) && session.isActive())
                .findFirst();
    }

    public void endSession(String sessionId) {
        findById(sessionId).ifPresent(Session::endSession);
    }
}
