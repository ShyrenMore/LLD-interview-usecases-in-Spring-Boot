package com.personal.lld.controller;

import com.personal.lld.domain.Song;
import com.personal.lld.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService service;

    @GetMapping
    public List<Song> get(@RequestParam String userId) {
        return service.getRecommendations(userId);
    }
}
