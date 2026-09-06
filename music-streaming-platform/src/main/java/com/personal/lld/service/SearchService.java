package com.personal.lld.service;

import com.personal.lld.domain.*;
import com.personal.lld.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    public SearchResponse search(String query, String type) {
        SearchResponse r = new SearchResponse();
        if (type == null || "SONG".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type))
            r.setSongs(songRepository.findByTitle(query));
        if (type == null || "ARTIST".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type))
            r.setArtists(artistRepository.findByName(query));
        if (type == null || "ALBUM".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type))
            r.setAlbums(albumRepository.findByTitle(query));
        return r;
    }

    @Data
    public static class SearchResponse {
        private List<Song> songs = new ArrayList<>();
        private List<Artist> artists = new ArrayList<>();
        private List<Album> albums = new ArrayList<>();
    }
}
