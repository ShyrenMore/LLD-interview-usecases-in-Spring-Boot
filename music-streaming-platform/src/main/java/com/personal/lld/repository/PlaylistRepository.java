package com.personal.lld.repository;

import com.personal.lld.domain.Playlist;

import java.util.*;

public interface PlaylistRepository {
    Playlist save(Playlist playlist);

    Optional<Playlist> findById(String id);

    Optional<Playlist> findByPlaylistId(String playlistId);

    List<Playlist> findByUserId(String userId);

    void delete(String playlistId);
}
