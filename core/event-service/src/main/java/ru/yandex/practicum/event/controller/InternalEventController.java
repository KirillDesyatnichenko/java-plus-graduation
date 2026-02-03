package ru.yandex.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.event.dto.EventInfoDto;
import ru.yandex.practicum.event.service.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventInfoDto getEventInfo(@PathVariable Long eventId) {
        return eventService.getEventInfo(eventId);
    }
}
