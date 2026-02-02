package ru.yandex.practicum.event.dto.request;

public record UserEventsQuery(Long userId, Integer from, Integer size) {
}
