package ru.yandex.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.service.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class UserActionListener {
    private final UserActionProcessor processor;

    @KafkaListener(topics = "${stats.topics.user-actions}",
            containerFactory = "userActionKafkaListenerContainerFactory")
    public void handle(UserActionAvro action) {
        if (action != null) {
            processor.handle(action);
        }
    }
}