package ru.yandex.practicum.collector.config;

import com.netflix.appinfo.ApplicationInfoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EurekaGrpcMetadataConfig {

    private final ApplicationInfoManager applicationInfoManager;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void publishGrpcPort() {
        try {
            String grpcPort = environment.getProperty("local.grpc.port");
            if (grpcPort == null || grpcPort.isBlank()) {
                log.warn("gRPC порт недоступен в свойстве local.grpc.port");
                return;
            }

            Map<String, String> currentMetadata = applicationInfoManager.getInfo().getMetadata();
            Map<String, String> metadata = currentMetadata == null ? new HashMap<>() : new HashMap<>(currentMetadata);
            metadata.put("gRPC_port", grpcPort);
            applicationInfoManager.registerAppMetadata(metadata);
            log.info("Опубликован gRPC_port={} в metadata Eureka", grpcPort);
        } catch (Exception e) {
            log.warn("Не удалось опубликовать metadata gRPC в Eureka", e);
        }
    }
}