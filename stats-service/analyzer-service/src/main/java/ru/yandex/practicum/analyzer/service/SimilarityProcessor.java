package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.analyzer.model.EventPairKey;
import ru.yandex.practicum.analyzer.model.EventSimilarity;
import ru.yandex.practicum.analyzer.repository.EventSimilarityRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SimilarityProcessor {
    private final EventSimilarityRepository repository;

    @Transactional
    public void handle(EventSimilarityAvro similarity) {
        long eventA = similarity.getEventA();
        long eventB = similarity.getEventB();
        if (eventA == eventB) {
            return;
        }
        long min = Math.min(eventA, eventB);
        long max = Math.max(eventA, eventB);
        EventPairKey key = new EventPairKey(min, max);

        EventSimilarity entity = repository.findById(key).orElse(null);
        Instant timestamp = similarity.getTimestamp() == null ? Instant.now() : similarity.getTimestamp();
        if (entity == null) {
            entity = new EventSimilarity(key, similarity.getScore(), timestamp);
        } else {
            entity.setScore(similarity.getScore());
            entity.setUpdatedAt(timestamp);
        }
        repository.save(entity);
    }
}