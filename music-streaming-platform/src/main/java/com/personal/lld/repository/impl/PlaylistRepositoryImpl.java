package com.personal.lld.repository.impl;

import com.personal.lld.domain.Playlist;
import com.personal.lld.repository.PlaylistRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class PlaylistRepositoryImpl implements PlaylistRepository {
    private final Map<String, Playlist> playlists = new ConcurrentHashMap<>();
    private final Map<String, String> idByPlaylistId = new ConcurrentHashMap<>();

    public Playlist save(Playlist p) {
        playlists.put(p.getId(), p);
        idByPlaylistId.put(p.getPlaylistId(), p.getId());
        return p;
    }

    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(playlists.get(id));
    }

    public Optional<Playlist> findByPlaylistId(String pid) {
        String id = idByPlaylistId.get(pid);
        return id == null ? Optional.empty() : Optional.ofNullable(playlists.get(id));
    }

    public List<Playlist> findByUserId(String u) {
        return playlists.values().stream().filter(p -> u.equals(p.getUserId())).collect(Collectors.toList());
    }

    public void delete(String pid) {
        String id = idByPlaylistId.remove(pid);
        if (id != null) playlists.remove(id);
    }
}
