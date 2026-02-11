package ru.yandex.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.service.SimilarityAggregator;

@Component
@RequiredArgsConstructor
public class UserActionListener {
    private final SimilarityAggregator aggregator;

    @KafkaListener(topics = "${stats.topics.user-actions}")
    public void handle(UserActionAvro action) {
        if (action != null) {
            aggregator.handle(action);
        }
    }
}