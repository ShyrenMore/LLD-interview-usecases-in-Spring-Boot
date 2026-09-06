package com.personal.lld.controller;

import com.personal.lld.domain.Download;
import com.personal.lld.service.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {
    private final DownloadService service;

    @PostMapping
    public Download download(@RequestParam String userId, @RequestParam String songId, @RequestParam String deviceId) {
        return service.download(userId, songId, deviceId);
    }

    @GetMapping
    public List<Download> get(@RequestParam String userId, @RequestParam(required = false) String deviceId) {
        return service.getDownloads(userId, deviceId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, @RequestParam String userId) {
        service.deleteDownload(id, userId);
    }
}
