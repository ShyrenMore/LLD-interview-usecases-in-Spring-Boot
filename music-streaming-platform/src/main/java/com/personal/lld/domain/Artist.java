package com.personal.lld.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    private String id, artistId, name, thumbnailUrl;
    private long createdAt;

    @Override
    public String toString() {
        return "Artist{" + "artistId='" + artistId + '\'' + ", name='" + name + '\'' + '}';
    }
}
