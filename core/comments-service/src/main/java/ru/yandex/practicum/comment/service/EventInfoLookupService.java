package ru.yandex.practicum.comment.service;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.event.client.EventClient;
import ru.yandex.practicum.event.dto.EventInfoDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;

@Service
@RequiredArgsConstructor
public class EventInfoLookupService {

    private final EventClient eventClient;

    @Retry(name = "event-service", fallbackMethod = "fallbackEvent")
    public EventInfoDto ensureEventExists(Long eventId) {
        try {
            return eventClient.getEventInfo(eventId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
    }

    public EventInfoDto fallbackEvent(Long eventId, Throwable ex) {
        throw new ValidationException("Event-service недоступен");
    }
}
