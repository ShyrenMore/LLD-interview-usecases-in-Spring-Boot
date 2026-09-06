package com.personal.lld.service.strategy;

import com.personal.lld.domain.*;
import com.personal.lld.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PopularityBasedStrategy implements RecommendationStrategy {
    private final SongRepository songRepository;

    public List<Song> generate(String uid, List<ListeningHistory> history) {
        return Collections.emptyList();
    }
}
