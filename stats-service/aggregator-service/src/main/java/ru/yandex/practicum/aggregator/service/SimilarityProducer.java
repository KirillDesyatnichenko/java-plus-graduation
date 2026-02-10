package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.aggregator.config.StatsTopicsProperties;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SimilarityProducer {
    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    private final StatsTopicsProperties topics;

    public void send(int eventA, int eventB, double score) {
        EventSimilarityAvro similarity = new EventSimilarityAvro();
        similarity.setEventA(eventA);
        similarity.setEventB(eventB);
        similarity.setScore(score);
        similarity.setTimestamp(Instant.now());

        long key = ((long) eventA) << 32 | (eventB & 0xffffffffL);
        kafkaTemplate.send(topics.getEventsSimilarity(), key, similarity);
    }
}