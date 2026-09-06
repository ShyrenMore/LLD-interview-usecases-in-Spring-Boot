package com.personal.lld.repository.impl;

import com.personal.lld.domain.Download;
import com.personal.lld.repository.DownloadRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DownloadRepositoryImpl implements DownloadRepository {
    private final Map<String, Download> downloads = new ConcurrentHashMap<>();
    private final Map<String, String> idByDownloadId = new ConcurrentHashMap<>();

    public Download save(Download d) {
        downloads.put(d.getId(), d);
        idByDownloadId.put(d.getDownloadId(), d.getId());
        return d;
    }

    public Optional<Download> findById(String id) {
        return Optional.ofNullable(downloads.get(id));
    }

    public List<Download> findByUserId(String u) {
        return downloads.values().stream().filter(d -> u.equals(d.getUserId())).collect(Collectors.toList());
    }

    public List<Download> findByUserIdAndDeviceId(String u, String dev) {
        return downloads.values().stream().filter(d -> u.equals(d.getUserId()) && dev.equals(d.getDeviceId())).collect(Collectors.toList());
    }

    public int countByUserId(String u) {
        return (int) downloads.values().stream().filter(d -> u.equals(d.getUserId())).count();
    }

    public void delete(String did) {
        String id = idByDownloadId.remove(did);
        if (id != null) downloads.remove(id);
    }

    public int countDistinctDevicesByUserId(String u) {
        return (int) downloads.values().stream().filter(d -> u.equals(d.getUserId())).map(Download::getDeviceId).distinct().count();
    }
}
