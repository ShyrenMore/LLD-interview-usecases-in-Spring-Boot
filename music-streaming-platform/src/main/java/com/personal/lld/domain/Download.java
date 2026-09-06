package com.personal.lld.domain;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class Download { private String id, downloadId, userId, songId, deviceId; private DownloadStatus downloadStatus; private String localFilePath; private long downloadedAt, createdAt; }
