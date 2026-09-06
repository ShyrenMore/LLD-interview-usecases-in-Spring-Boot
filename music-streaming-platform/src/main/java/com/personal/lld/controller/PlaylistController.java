package com.personal.lld.controller;

import com.personal.lld.domain.Playlist;
import com.personal.lld.service.PlaylistService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService service;

    @PostMapping
    public Playlist create(@RequestParam String userId, @RequestParam String name, @RequestBody(required = false) List<String> songIds) {
        return service.createPlaylist(userId, name, songIds);
    }

    @PutMapping("/{id}")
    public Playlist update(@PathVariable String id, @RequestParam String userId, @RequestParam(required = false) String name, @RequestBody(required = false) List<String> songIds) {
        return service.updatePlaylist(id, userId, name, songIds);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, @RequestParam String userId) {
        service.deletePlaylist(id, userId);
    }

    @PostMapping("/{id}/songs")
    public Playlist add(@PathVariable String id, @RequestParam String userId, @RequestBody List<String> ids) {
        return service.addSongs(id, userId, ids);
    }

    @DeleteMapping("/{id}/songs")
    public Playlist remove(@PathVariable String id, @RequestParam String userId, @RequestBody List<String> ids) {
        return service.removeSongs(id, userId, ids);
    }
}
