package com.personal.lld.repository.impl;

import com.personal.lld.domain.ListeningHistory;
import com.personal.lld.repository.ListeningHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ListeningHistoryRepositoryImpl implements ListeningHistoryRepository {
    private final Map<String, ListeningHistory> byId = new ConcurrentHashMap<>();
    private final List<ListeningHistory> list = new CopyOnWriteArrayList<>();

    public void save(ListeningHistory h) {
        byId.put(h.getId(), h);
        if (list.stream().noneMatch(x -> x.getId().equals(h.getId()))) list.add(h);
    }

    public List<ListeningHistory> findByUserId(String u) {
        return list.stream().filter(h -> u.equals(h.getUserId())).collect(Collectors.toList());
    }

    public List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(String u, int limit) {
        return list.stream().filter(h -> u.equals(h.getUserId())).sorted(Comparator.comparingLong(ListeningHistory::getPlayedAt).reversed()).limit(limit).collect(Collectors.toList());
    }

    public ListeningHistory findByUserIdAndSongIdAndDate(String u, String s, long date) {
        long day = 24 * 60 * 60 * 1000L;
        long start = date - (date % day);
        return list.stream().filter(h -> u.equals(h.getUserId()) && s.equals(h.getSongId()) && h.getPlayedAt() >= start && h.getPlayedAt() < start + day).findFirst().orElse(null);
    }
}
