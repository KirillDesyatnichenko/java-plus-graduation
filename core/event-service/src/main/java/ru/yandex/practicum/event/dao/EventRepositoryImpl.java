package ru.yandex.practicum.event.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.event.dto.enums.EventSort;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.dto.enums.EventState;
import ru.yandex.practicum.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> searchEventsByAdmin(AdminEventFilter filter) {
        QEvent event = QEvent.event;

        BooleanBuilder where = new BooleanBuilder();

        if (filter.getUsers() != null && !filter.getUsers().isEmpty()) {
            where.and(event.initiatorId.in(filter.getUsers()));
        }

        if (filter.getStates() != null && !filter.getStates().isEmpty()) {
            where.and(event.state.in(filter.getStates()));
        }

        if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
            where.and(event.category.id.in(filter.getCategories()));
        }

        if (filter.getRangeStart() != null) {
            where.and(event.eventDate.after(filter.getRangeStart()));
        }

        if (filter.getRangeEnd() != null) {
            where.and(event.eventDate.before(filter.getRangeEnd()));
        }

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.category).fetchJoin()
                .where(where)
                .orderBy(event.eventDate.desc())
                .offset(filter.getFrom())
                .limit(filter.getSize())
                .fetch();

    }

    @Override
    public List<Event> searchEventsByPublic(PublicEventFilter filter) {
        QEvent event = QEvent.event;
        BooleanBuilder where = new BooleanBuilder();

        where.and(event.state.eq(EventState.PUBLISHED));

        if (filter.getText() != null && !filter.getText().isBlank()) {
            String search = filter.getText().toLowerCase();
            where.and(
                    event.annotation.lower().contains(search)
                            .or(event.description.lower().contains(search))
            );
        }

        if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
            where.and(event.category.id.in(filter.getCategories()));
        }

        if (filter.getPaid() != null) {
            where.and(event.paid.eq(filter.getPaid()));
        }

        LocalDateTime start = filter.getRangeStart() != null ? filter.getRangeStart() : LocalDateTime.now();
        if (filter.getRangeEnd() != null) {
            where.and(event.eventDate.between(start, filter.getRangeEnd()));
        } else {
            where.and(event.eventDate.after(start));
        }

        JPQLQuery<Event> query = queryFactory
                .selectFrom(event)
                .leftJoin(event.category).fetchJoin()
                .where(where);

        if (filter.getSort() == null || filter.getSort() == EventSort.EVENT_DATE) {
            query.orderBy(event.eventDate.asc());
        }

        return query
                .offset(filter.getFrom())
                .limit(filter.getSize())
                .fetch();
    }
}