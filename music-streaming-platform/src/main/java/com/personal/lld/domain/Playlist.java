package com.personal.lld.domain;

import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
public class Playlist {
    private String id, playlistId, name, userId;
    private boolean isPublic;
    private List<String> songIds = new ArrayList<>();
    private long createdAt, updatedAt;

    public Playlist(String id, String playlistId, String name, String userId, boolean isPublic, List<String> songIds, long createdAt, long updatedAt) {
        this.id = id;
        this.playlistId = playlistId;
        this.name = name;
        this.userId = userId;
        this.isPublic = isPublic;
        this.songIds = songIds != null ? new ArrayList<>(songIds) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void setSongIds(List<String> ids) {
        this.songIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
    }
}
