package ru.yandex.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.analyzer.service.SimilarityProcessor;

@Component
@RequiredArgsConstructor
public class EventSimilarityListener {
    private final SimilarityProcessor processor;

    @KafkaListener(topics = "${stats.topics.events-similarity}",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory")
    public void handle(EventSimilarityAvro similarity) {
        if (similarity != null) {
            processor.handle(similarity);
        }
    }
}