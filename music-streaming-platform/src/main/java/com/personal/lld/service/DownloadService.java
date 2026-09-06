package com.personal.lld.service;

import com.personal.lld.domain.*;
import com.personal.lld.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private static final int MAX_DEVICES = 5, MAX_DOWNLOADS = 10000;
    private final DownloadRepository downloadRepository;
    private final UserRepository userRepository;

    public Download download(String uid, String sid, String did) {
        User u = userRepository.findById(uid).orElseThrow(() -> new IllegalArgumentException("User not found: " + uid));
        if (u.getSubscriptionTier() != SubscriptionTier.PREMIUM)
            throw new IllegalArgumentException("Premium subscription required for downloads");
        if (!validateDeviceLimit(uid, did))
            throw new IllegalArgumentException("Device limit exceeded. Max " + MAX_DEVICES + " devices allowed");
        if (downloadRepository.countByUserId(uid) >= MAX_DOWNLOADS)
            throw new IllegalArgumentException("Download limit exceeded. Max " + MAX_DOWNLOADS + " songs allowed");
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Download d = new Download(id, "DL_" + id.substring(0, 8), uid, sid, did, DownloadStatus.COMPLETED, "/cache/" + did + "/" + sid, now, now);
        return downloadRepository.save(d);
    }

    public List<Download> getDownloads(String uid, String did) {
        return did != null ? downloadRepository.findByUserIdAndDeviceId(uid, did) : downloadRepository.findByUserId(uid);
    }

    public void deleteDownload(String did, String uid) {
        Download d = downloadRepository.findById(did).orElseThrow(() -> new IllegalArgumentException("Download not found: " + did));
        if (!uid.equals(d.getUserId())) throw new IllegalArgumentException("User does not own this download");
        downloadRepository.delete(d.getDownloadId());
    }

    private boolean validateDeviceLimit(String u, String d) {
        if (!downloadRepository.findByUserIdAndDeviceId(u, d).isEmpty()) return true;
        return downloadRepository.countDistinctDevicesByUserId(u) < MAX_DEVICES;
    }
}
