package com.personal.lld.repository.impl;

import com.personal.lld.domain.Song;
import com.personal.lld.repository.SongRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SongRepositoryImpl implements SongRepository {
    private final Map<String, Song> songs = new ConcurrentHashMap<>();
    private final Map<String, String> idBySongId = new ConcurrentHashMap<>();

    public Song save(Song s) {
        songs.put(s.getId(), s);
        idBySongId.put(s.getSongId(), s.getId());
        return s;
    }

    public Optional<Song> findById(String id) {
        return Optional.ofNullable(songs.get(id));
    }

    public Optional<Song> findBySongId(String sid) {
        String id = idBySongId.get(sid);
        return id == null ? Optional.empty() : Optional.ofNullable(songs.get(id));
    }

    public List<Song> findByTitle(String q) {
        String x = q.toLowerCase();
        return songs.values().stream().filter(s -> s.getTitle().toLowerCase().contains(x)).collect(Collectors.toList());
    }

    public List<Song> findByArtistId(String a) {
        return songs.values().stream().filter(s -> a.equals(s.getArtistId())).collect(Collectors.toList());
    }

    public List<Song> findByAlbumId(String a) {
        return songs.values().stream().filter(s -> a != null && a.equals(s.getAlbumId())).collect(Collectors.toList());
    }

    public List<Song> findByGenre(String g) {
        String x = g.toLowerCase();
        return songs.values().stream().filter(s -> s.getGenre().toLowerCase().contains(x)).collect(Collectors.toList());
    }

    public List<Song> findAllByIds(List<String> ids) {
        return ids.stream().map(this::findBySongId).flatMap(Optional::stream).collect(Collectors.toList());
    }
}
