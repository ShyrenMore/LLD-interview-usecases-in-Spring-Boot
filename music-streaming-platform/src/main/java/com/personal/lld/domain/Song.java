package com.personal.lld.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {
    private String id, songId, title, artistId, albumId;
    private long duration;
    private String genre, audioUrl, thumbnailUrl;
    private long fileSize;
    private AudioQuality quality;
    private AudioFormat format;
    private long createdAt;

    @Override
    public String toString() {
        return "Song{" + "songId='" + songId + '\'' + ", title='" + title + '\'' + ", artistId='" + artistId + '\'' + ", duration=" + duration + '}';
    }
}
