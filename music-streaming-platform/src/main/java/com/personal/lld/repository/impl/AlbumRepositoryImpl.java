package com.personal.lld.repository.impl;

import com.personal.lld.domain.Album;
import com.personal.lld.repository.AlbumRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AlbumRepositoryImpl implements AlbumRepository {
    private final Map<String, Album> albums = new ConcurrentHashMap<>();
    private final Map<String, String> idByAlbumId = new ConcurrentHashMap<>();

    public Album save(Album a) {
        albums.put(a.getId(), a);
        idByAlbumId.put(a.getAlbumId(), a.getId());
        return a;
    }

    public Optional<Album> findById(String id) {
        return Optional.ofNullable(albums.get(id));
    }

    public Optional<Album> findByAlbumId(String aid) {
        String id = idByAlbumId.get(aid);
        return id == null ? Optional.empty() : Optional.ofNullable(albums.get(id));
    }

    public List<Album> findByArtistId(String a) {
        return albums.values().stream().filter(x -> a.equals(x.getArtistId())).collect(Collectors.toList());
    }

    public List<Album> findByTitle(String q) {
        String x = q.toLowerCase();
        return albums.values().stream().filter(a -> a.getTitle().toLowerCase().contains(x)).collect(Collectors.toList());
    }
}
