package ru.yandex.practicum.analyzer.config;

import com.netflix.appinfo.ApplicationInfoManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EurekaGrpcMetadataConfig {

    private final ApplicationInfoManager applicationInfoManager;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void publishGrpcPort() {
        String grpcPort = environment.getProperty("local.grpc.port");
        if (grpcPort == null || grpcPort.isBlank()) {
            return;
        }

        Map<String, String> metadata = new HashMap<>(applicationInfoManager.getInfo().getMetadata());
        metadata.put("gRPC_port", grpcPort);
        applicationInfoManager.registerAppMetadata(metadata);
    }
}