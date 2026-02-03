package ru.yandex.practicum.request.service;

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

    @Retry(name = "event-service", fallbackMethod = "fallbackEventInfo")
    public EventInfoDto fetchEventInfo(Long eventId) {
        try {
            return eventClient.getEventInfo(eventId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
    }

    public EventInfoDto fallbackEventInfo(Long eventId, Throwable ex) {
        throw new ValidationException("Event-service недоступен");
    }
}
