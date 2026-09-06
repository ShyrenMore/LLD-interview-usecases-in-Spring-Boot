package com.personal.lld.repository;

import com.personal.lld.domain.ListeningHistory;

import java.util.*;

public interface ListeningHistoryRepository {
    void save(ListeningHistory history);

    List<ListeningHistory> findByUserId(String userId);

    List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(String userId, int limit);

    ListeningHistory findByUserIdAndSongIdAndDate(String userId, String songId, long date);
}
