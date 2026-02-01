package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@Validated
@AllArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEventsByAdmin(
            @Valid AdminEventFilter filter) {
        return eventService.searchEventsByAdmin(filter);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto moderateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest adminRequest) {
        return eventService.moderateEvent(eventId, adminRequest);
    }
}
