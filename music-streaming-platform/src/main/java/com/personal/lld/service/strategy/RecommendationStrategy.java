package com.personal.lld.service.strategy;

import com.personal.lld.domain.*;

import java.util.*;

public interface RecommendationStrategy {
    List<Song> generate(String userId, List<ListeningHistory> history);
}
