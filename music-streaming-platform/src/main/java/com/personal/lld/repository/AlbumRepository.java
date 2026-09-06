package com.personal.lld.repository;

import com.personal.lld.domain.Album;

import java.util.*;

public interface AlbumRepository {
    Album save(Album album);

    Optional<Album> findById(String id);

    Optional<Album> findByAlbumId(String albumId);

    List<Album> findByArtistId(String artistId);

    List<Album> findByTitle(String title);
}
