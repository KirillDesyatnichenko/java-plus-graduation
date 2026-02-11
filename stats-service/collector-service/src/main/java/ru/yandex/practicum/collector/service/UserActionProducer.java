package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.collector.config.StatsTopicsProperties;

@Service
@RequiredArgsConstructor
public class UserActionProducer {
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;
    private final StatsTopicsProperties topics;

    public void send(UserActionAvro action) {
        kafkaTemplate.send(topics.getUserActions(), (long) action.getUserId(), action);
    }
}