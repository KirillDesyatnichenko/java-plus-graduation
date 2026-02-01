package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.yandex.practicum.client.exception.StatsClientException;
import ru.yandex.practicum.client.exception.StatsServerUnavailable;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.net.URI;

@Slf4j
@Service
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private final String statsServiceId;
    private final RetryTemplate retryTemplate;

    public StatsClient(DiscoveryClient discoveryClient,
                       @Value("${stats.service.id:stats-server}") String statsServiceId) {
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.restClient = RestClient.builder().build();
        this.retryTemplate = buildRetryTemplate();
    }

    public void saveHit(EndpointHitDto hitDto) {
        try {
            handleErrors(restClient.post()
                    .uri(makeUri("/hit"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hitDto))
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Не удалось отправить хит в StatsService", e);
            throw new StatsClientException("Не удалось отправить хит в StatsService", e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        try {
            URI baseUri = makeUri("/stats");
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri)
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER))
                    .queryParam("unique", unique);
            if (uris != null && !uris.isEmpty()) {
                for (String u : uris) {
                    builder.queryParam("uris", u);
                }
            }
            URI uri = builder.build(true).toUri();

            return handleErrors(restClient.get().uri(uri))
                    .body(new org.springframework.core.ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });
        } catch (RestClientException e) {
            log.error("Не удалось получить статистику из StatsService", e);
            throw new StatsClientException("Не удалось получить статистику из StatsService", e);
        }
    }

    private RestClient.ResponseSpec handleErrors(RestClient.RequestHeadersSpec<?> request) {
        return request.retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    String errorBody = readBodyAsString(res);
                    throw new StatsClientException("Ошибка клиента (4xx) при обращении к StatsService: "
                            + (errorBody.isBlank() ? "сообщение ошибки не предоставлено" : errorBody));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    String errorBody = readBodyAsString(res);
                    throw new StatsClientException("Ошибка сервера (5xx) в StatsService: "
                            + (errorBody.isBlank() ? "сообщение ошибки не предоставлено" : errorBody));
                });
    }

    private String readBodyAsString(ClientHttpResponse res) {
        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(res.getBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(java.util.stream.Collectors.joining("\n"));
        } catch (Exception e) {
            return "не удалось прочитать тело ответа";
        }
    }

    private RetryTemplate buildRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        template.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        template.setRetryPolicy(retryPolicy);
        return template;
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(context -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }
}