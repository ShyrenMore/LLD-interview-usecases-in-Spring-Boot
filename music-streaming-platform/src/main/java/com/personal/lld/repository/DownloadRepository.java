package com.personal.lld.repository;

import com.personal.lld.domain.Download;

import java.util.*;

public interface DownloadRepository {
    Download save(Download download);

    Optional<Download> findById(String id);

    List<Download> findByUserId(String userId);

    List<Download> findByUserIdAndDeviceId(String userId, String deviceId);

    int countByUserId(String userId);

    void delete(String downloadId);

    int countDistinctDevicesByUserId(String userId);
}
