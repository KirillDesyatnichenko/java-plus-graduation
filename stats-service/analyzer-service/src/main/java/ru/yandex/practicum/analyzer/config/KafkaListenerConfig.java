package ru.yandex.practicum.analyzer.config;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.avro.EventSimilarityAvroDeserializer;
import ru.yandex.practicum.stats.avro.UserActionAvroDeserializer;

import java.util.Map;

@Configuration
public class KafkaListenerConfig {

    @Bean
    public ConsumerFactory<Long, UserActionAvro> userActionConsumerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildConsumerProperties(null);
        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), new UserActionAvroDeserializer());
    }

    @Bean
    public ConsumerFactory<Long, EventSimilarityAvro> eventSimilarityConsumerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildConsumerProperties(null);
        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), new EventSimilarityAvroDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> userActionKafkaListenerContainerFactory(
            ConsumerFactory<Long, UserActionAvro> userActionConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userActionConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EventSimilarityAvro> eventSimilarityKafkaListenerContainerFactory(
            ConsumerFactory<Long, EventSimilarityAvro> eventSimilarityConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventSimilarityConsumerFactory);
        return factory;
    }
}