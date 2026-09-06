package com.personal.lld.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

@Service
public class LockService {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public boolean acquire(String key, long timeoutMs) {
        ReentrantLock l = locks.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            return l.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void release(String key) {
        ReentrantLock l = locks.get(key);
        if (l != null && l.isHeldByCurrentThread()) l.unlock();
    }
}
