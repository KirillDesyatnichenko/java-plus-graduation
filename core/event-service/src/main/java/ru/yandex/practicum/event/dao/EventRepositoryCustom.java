package ru.yandex.practicum.event.dao;

import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.model.Event;

import java.util.List;

public interface EventRepositoryCustom {
    List<Event> searchEventsByAdmin(AdminEventFilter filter);

    List<Event> searchEventsByPublic(PublicEventFilter filter);
}
