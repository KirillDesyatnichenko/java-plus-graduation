package ru.yandex.practicum.collector.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StatsTopicsProperties.class)
public class CollectorConfig {
}
