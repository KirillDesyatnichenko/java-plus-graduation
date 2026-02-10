package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "stats.topics")
public class StatsTopicsProperties {
    private String userActions;
    private String eventsSimilarity;
}