package com.personal.lld.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private String id, albumId, title, artistId, thumbnailUrl;
    private long createdAt;

    @Override
    public String toString() {
        return "Album{" + "albumId='" + albumId + '\'' + ", title='" + title + '\'' + ", artistId='" + artistId + '\'' + '}';
    }
}
