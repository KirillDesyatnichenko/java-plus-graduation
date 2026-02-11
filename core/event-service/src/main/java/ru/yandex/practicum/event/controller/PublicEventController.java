package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@Validated
@AllArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> searchPublicEvents(
            @Valid PublicEventFilter filter) {
        return eventService.searchPublicEvents(filter);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @RequestParam(defaultValue = "10") int size) {
        return eventService.getRecommendationsForUser(userId, size);
    }

    @GetMapping("/{id}")
    public EventFullDto findPublicEventById(@PathVariable long id,
                                            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        return eventService.findPublicEventById(id, userId);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable long eventId,
                          @RequestHeader("X-EWM-USER-ID") long userId) {
        eventService.likeEvent(userId, eventId);
    }
}
