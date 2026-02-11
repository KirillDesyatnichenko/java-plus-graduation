package ru.yandex.practicum.analyzer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({StatsTopicsProperties.class, ActionWeightsProperties.class, RecommendationProperties.class})
public class AnalyzerConfig {
}