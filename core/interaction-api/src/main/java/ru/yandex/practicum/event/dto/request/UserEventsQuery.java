package ru.yandex.practicum.event.dto.request;

public record UserEventsQuery(long userId, int from, int size) {
}
