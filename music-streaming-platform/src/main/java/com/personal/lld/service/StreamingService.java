package com.personal.lld.service;

import com.personal.lld.repository.SongRepository;
import com.personal.lld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public String getStreamUrl(String songId, String userId) {
        validateAccess(songId, userId);
        return "https://stream.example.com/songs/" + songId;
    }

    public byte[] getChunk(String songId, long start, long end) {
        if (start < 0 || end < start) throw new IllegalArgumentException("Invalid byte range");
        songRepository.findBySongId(songId).orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
        Optional<byte[]> cached = cacheService.getChunk(songId, start, end);
        if (cached.isPresent()) return cached.get();
        byte[] data = ("AUDIO_CHUNK:" + songId + ":" + start + ":" + end).getBytes(StandardCharsets.UTF_8);
        cacheService.putChunk(songId, start, end, data);
        return data;
    }

    private void validateAccess(String songId, String userId) {
        songRepository.findBySongId(songId).orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
