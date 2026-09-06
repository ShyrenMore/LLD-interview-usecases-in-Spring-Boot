package com.personal.lld.service.strategy;

import com.personal.lld.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CollaborativeFilteringStrategy implements RecommendationStrategy {
    public List<Song> generate(String uid, List<ListeningHistory> history) {
        return Collections.emptyList();
    }
}
