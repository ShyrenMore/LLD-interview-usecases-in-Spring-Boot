package com.personal.lld.service;

import com.personal.lld.domain.*;
import com.personal.lld.repository.ListeningHistoryRepository;
import com.personal.lld.service.strategy.RecommendationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {
    private final ListeningHistoryRepository listeningHistoryRepository;
    private RecommendationStrategy strategy;

    public RecommendationService(ListeningHistoryRepository r, RecommendationStrategy s) {
        listeningHistoryRepository = r;
        strategy = s;
    }

    public void setStrategy(RecommendationStrategy s) {
        strategy = s;
    }

    public List<Song> getRecommendations(String uid) {
        return strategy.generate(uid, listeningHistoryRepository.findByUserId(uid));
    }
}
