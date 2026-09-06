package com.personal.lld.service;

import com.personal.lld.domain.*;
import com.personal.lld.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final LockService lockService;

    public Playlist createPlaylist(String userId, String name, List<String> ids) {
        validateSongs(ids);
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        return playlistRepository.save(new Playlist(id, "PL_" + id.substring(0, 8), name, userId, false, ids, now, now));
    }

    public Playlist updatePlaylist(String pid, String uid, String name, List<String> ids) {
        String k = "playlist_lock_" + pid;
        if (!lockService.acquire(k, 500))
            throw new IllegalStateException("Playlist is being updated by another request");
        try {
            Playlist p = getOwned(pid, uid);
            validateSongs(ids);
            if (name != null) p.setName(name);
            if (ids != null) p.setSongIds(ids);
            p.setUpdatedAt(System.currentTimeMillis());
            return playlistRepository.save(p);
        } finally {
            lockService.release(k);
        }
    }

    public void deletePlaylist(String pid, String uid) {
        playlistRepository.delete(getOwned(pid, uid).getPlaylistId());
    }

    public Playlist addSongs(String pid, String uid, List<String> ids) {
        validateSongs(ids);
        String k = "playlist_lock_" + pid;
        if (!lockService.acquire(k, 500))
            throw new IllegalStateException("Playlist is being updated by another request");
        try {
            Playlist p = getOwned(pid, uid);
            List<String> x = new ArrayList<>(p.getSongIds());
            ids.forEach(i -> {
                if (!x.contains(i)) x.add(i);
            });
            p.setSongIds(x);
            p.setUpdatedAt(System.currentTimeMillis());
            return playlistRepository.save(p);
        } finally {
            lockService.release(k);
        }
    }

    public Playlist removeSongs(String pid, String uid, List<String> ids) {
        String k = "playlist_lock_" + pid;
        if (!lockService.acquire(k, 500))
            throw new IllegalStateException("Playlist is being updated by another request");
        try {
            Playlist p = getOwned(pid, uid);
            List<String> x = new ArrayList<>(p.getSongIds());
            x.removeAll(ids);
            p.setSongIds(x);
            p.setUpdatedAt(System.currentTimeMillis());
            return playlistRepository.save(p);
        } finally {
            lockService.release(k);
        }
    }

    private Playlist getOwned(String pid, String uid) {
        Playlist p = playlistRepository.findByPlaylistId(pid).orElseThrow(() -> new IllegalArgumentException("Playlist not found: " + pid));
        if (!uid.equals(p.getUserId())) throw new IllegalArgumentException("User does not own this playlist");
        return p;
    }

    private void validateSongs(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        if (songRepository.findAllByIds(ids).size() != ids.size())
            throw new IllegalArgumentException("Some songs not found");
    }
}
