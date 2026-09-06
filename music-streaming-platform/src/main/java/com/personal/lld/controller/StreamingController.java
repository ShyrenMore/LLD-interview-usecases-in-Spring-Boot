package com.personal.lld.controller;

import com.personal.lld.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {
    private final StreamingService service;

    @GetMapping("/{songId}")
    public byte[] stream(@PathVariable String songId, @RequestParam long start, @RequestParam long end, @RequestParam String userId) {
        return service.getChunk(songId, start, end);
    }
}
