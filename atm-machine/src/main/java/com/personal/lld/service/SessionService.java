package com.personal.lld.service;

import com.personal.lld.domain.Session;
import com.personal.lld.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;

    public Session startSession(String atmId, String cardId) {
        // TODO: Get account ID from bank servers.
        String accountId = "ACC_001";

        Session session = new Session(
                "SESSION_" + System.currentTimeMillis(),
                atmId,
                cardId,
                accountId);

        return sessionRepository.save(session);
    }

    public void endSession(String sessionId) {
        sessionRepository.endSession(sessionId);
    }

    public Session getCurrentSession(String atmId) {
        return sessionRepository.findActiveByATM(atmId).orElse(null);
    }

    public void handleSessionTimeout(String sessionId) {
        // TODO: Implement session timeout logic
        endSession(sessionId);
    }

    public Session getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }
}
