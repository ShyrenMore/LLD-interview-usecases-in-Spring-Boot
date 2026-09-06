package com.personal.lld.repository;

import com.personal.lld.domain.Artist;

import java.util.*;

public interface ArtistRepository {
    Artist save(Artist artist);

    Optional<Artist> findById(String id);

    Optional<Artist> findByArtistId(String artistId);

    List<Artist> findByName(String name);
}
