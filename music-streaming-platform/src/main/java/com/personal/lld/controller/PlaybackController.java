package com.personal.lld.controller;

import com.personal.lld.domain.*;
import com.personal.lld.service.PlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playback")
@RequiredArgsConstructor
public class PlaybackController {
    private final PlaybackService service;

    @PostMapping("/play")
    public PlaybackService.PlaybackStateResponse play(@RequestParam String userId, @RequestParam PlaybackSource sourceType, @RequestParam String sourceId, @RequestParam(required = false) Long startPosition) {
        return service.play(userId, sourceType, sourceId, startPosition);
    }

    @PostMapping("/{sessionId}/pause")
    public PlaybackService.PlaybackStateResponse pause(@PathVariable String sessionId) {
        return service.pause(sessionId);
    }

    @PostMapping("/{sessionId}/resume")
    public PlaybackService.PlaybackStateResponse resume(@PathVariable String sessionId) {
        return service.resume(sessionId);
    }

    @PostMapping("/{sessionId}/next")
    public PlaybackService.PlaybackStateResponse next(@PathVariable String sessionId) {
        return service.skipNext(sessionId);
    }

    @PostMapping("/{sessionId}/previous")
    public PlaybackService.PlaybackStateResponse previous(@PathVariable String sessionId) {
        return service.skipPrevious(sessionId);
    }

    @GetMapping("/{sessionId}")
    public PlaybackService.PlaybackStateResponse state(@PathVariable String sessionId) {
        return service.getState(sessionId);
    }

    @PostMapping("/{sessionId}/shuffle")
    public PlaybackService.PlaybackStateResponse shuffle(@PathVariable String sessionId, @RequestParam boolean enabled) {
        return service.toggleShuffle(sessionId, enabled);
    }

    @PostMapping("/{sessionId}/repeat")
    public PlaybackService.PlaybackStateResponse repeat(@PathVariable String sessionId, @RequestParam RepeatMode mode) {
        return service.setRepeatMode(sessionId, mode);
    }

    @PostMapping("/{sessionId}/position")
    public void position(@PathVariable String sessionId, @RequestParam long position) {
        service.updatePosition(sessionId, position);
    }
}
