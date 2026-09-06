package com.personal.lld.service.strategy;

import com.personal.lld.domain.*;
import com.personal.lld.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class GenreBasedStrategy implements RecommendationStrategy {
    private final SongRepository songRepository;

    public List<Song> generate(String uid, List<ListeningHistory> history) {
        if (history == null || history.isEmpty()) return Collections.emptyList();
        Map<String, Long> counts = new HashMap<>();
        for (ListeningHistory h : history)
            songRepository.findBySongId(h.getSongId()).ifPresent(s -> counts.merge(s.getGenre(), 1L, Long::sum));
        Optional<String> top = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
        return top.map(g -> songRepository.findByGenre(g).stream().limit(10).collect(Collectors.toList())).orElse(Collections.emptyList());
    }
}
