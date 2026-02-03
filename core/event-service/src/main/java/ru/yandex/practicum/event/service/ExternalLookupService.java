package ru.yandex.practicum.event.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.request.client.RequestClient;
import ru.yandex.practicum.request.client.dto.ConfirmedRequestCountDto;
import ru.yandex.practicum.request.client.dto.RequestCountDto;
import ru.yandex.practicum.user.client.UserClient;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalLookupService {

    private final UserClient userClient;
    private final RequestClient requestClient;

    @Retry(name = "user-service", fallbackMethod = "fallbackUserShorts")
    public Map<Long, UserShortDto> fetchUserShorts(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> uniqueIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) {
            return Map.of();
        }
        List<UserShortDto> users = userClient.getUsersShort(uniqueIds);
        if (users == null) {
            return Map.of();
        }
        return users.stream().collect(Collectors.toMap(UserShortDto::getId, u -> u));
    }

    public Map<Long, UserShortDto> fallbackUserShorts(List<Long> userIds, Throwable ex) {
        log.warn("User-service недоступен, используем заглушки: {}", ex.getMessage());
        if (userIds == null) {
            return Map.of();
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(id -> id, this::fallbackUser));
    }

    @Retry(name = "request-service", fallbackMethod = "fallbackConfirmedCounts")
    public Map<Long, Long> fetchConfirmedCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        List<Long> uniqueIds = eventIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) {
            return Map.of();
        }
        List<ConfirmedRequestCountDto> counts = requestClient.getConfirmedCounts(uniqueIds);
        if (counts == null) {
            return Map.of();
        }
        return counts.stream().collect(Collectors.toMap(ConfirmedRequestCountDto::getEventId,
                c -> c.getCount() == null ? 0L : c.getCount()));
    }

    public Map<Long, Long> fallbackConfirmedCounts(List<Long> eventIds, Throwable ex) {
        log.warn("Request-service недоступен, подтвержденные заявки = 0: {}", ex.getMessage());
        if (eventIds == null) {
            return Map.of();
        }
        return eventIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> 0L));
    }

    @Retry(name = "request-service", fallbackMethod = "fallbackRequestCounts")
    public Map<Long, Long> fetchRequestCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        List<Long> uniqueIds = eventIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) {
            return Map.of();
        }
        List<RequestCountDto> counts = requestClient.getRequestCounts(uniqueIds);
        if (counts == null) {
            return Map.of();
        }
        return counts.stream().collect(Collectors.toMap(RequestCountDto::getEventId,
                c -> c.getCount() == null ? 0L : c.getCount()));
    }

    public Map<Long, Long> fallbackRequestCounts(List<Long> eventIds, Throwable ex) {
        log.warn("Request-service недоступен, заявки = 0: {}", ex.getMessage());
        if (eventIds == null) {
            return Map.of();
        }
        return eventIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> 0L));
    }

    private UserShortDto fallbackUser(Long userId) {
        UserShortDto dto = new UserShortDto();
        dto.setId(userId == null ? 0L : userId);
        dto.setName("unknown");
        return dto;
    }
}
