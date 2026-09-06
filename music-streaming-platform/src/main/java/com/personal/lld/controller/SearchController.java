package com.personal.lld.controller;

import com.personal.lld.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;

    @GetMapping
    public SearchService.SearchResponse search(@RequestParam String query, @RequestParam(required = false, defaultValue = "ALL") String type) {
        return service.search(query, type);
    }
}
