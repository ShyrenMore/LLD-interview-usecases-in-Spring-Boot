package com.personal.lld.repository.impl;

import com.personal.lld.domain.Artist;
import com.personal.lld.repository.ArtistRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ArtistRepositoryImpl implements ArtistRepository {
    private final Map<String, Artist> artists = new ConcurrentHashMap<>();
    private final Map<String, String> idByArtistId = new ConcurrentHashMap<>();

    public Artist save(Artist a) {
        artists.put(a.getId(), a);
        idByArtistId.put(a.getArtistId(), a.getId());
        return a;
    }

    public Optional<Artist> findById(String id) {
        return Optional.ofNullable(artists.get(id));
    }

    public Optional<Artist> findByArtistId(String aid) {
        String id = idByArtistId.get(aid);
        return id == null ? Optional.empty() : Optional.ofNullable(artists.get(id));
    }

    public List<Artist> findByName(String q) {
        String x = q.toLowerCase();
        return artists.values().stream().filter(a -> a.getName().toLowerCase().contains(x)).collect(Collectors.toList());
    }
}
