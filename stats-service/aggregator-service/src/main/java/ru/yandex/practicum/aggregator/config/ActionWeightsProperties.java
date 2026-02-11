package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "stats.actions")
public class ActionWeightsProperties {
    private double view = 1.0;
    private double register = 2.0;
    private double like = 3.0;
}