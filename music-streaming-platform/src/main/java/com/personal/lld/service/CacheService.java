package com.personal.lld.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int maxSize = 1000;

    private static class CacheEntry {
        final byte[] data;
        volatile long lastAccessed;

        CacheEntry(byte[] d) {
            data = d;
            lastAccessed = System.currentTimeMillis();
        }
    }

    public Optional<byte[]> getChunk(String songId, long start, long end) {
        CacheEntry e = cache.get(key(songId, start, end));
        if (e == null) return Optional.empty();
        e.lastAccessed = System.currentTimeMillis();
        return Optional.of(e.data);
    }

    public synchronized void putChunk(String songId, long start, long end, byte[] chunk) {
        String k = key(songId, start, end);
        if (!cache.containsKey(k) && cache.size() >= maxSize) evictLRU();
        cache.put(k, new CacheEntry(chunk));
    }

    public void evictChunk(String s, long a, long b) {
        cache.remove(key(s, a, b));
    }

    public void evictSong(String s) {
        cache.keySet().removeIf(k -> k.startsWith(s + "_"));
    }

    private void evictLRU() {
        cache.entrySet().stream().min(Comparator.comparingLong(e -> e.getValue().lastAccessed)).ifPresent(e -> cache.remove(e.getKey()));
    }

    private String key(String s, long a, long b) {
        return s + "_" + a + "_" + b;
    }
}
