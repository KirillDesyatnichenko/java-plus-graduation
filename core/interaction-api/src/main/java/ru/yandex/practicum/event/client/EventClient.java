package ru.yandex.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.event.dto.EventInfoDto;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/events/{eventId}")
    EventInfoDto getEventInfo(@PathVariable Long eventId);
}
