package ru.yandex.practicum.aggregator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({StatsTopicsProperties.class, ActionWeightsProperties.class})
public class AggregatorConfig {
}