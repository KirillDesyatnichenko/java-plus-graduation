package ru.yandex.practicum.event.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.dto.enums.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    @EntityGraph(attributePaths = "category")
    List<Event> findByInitiatorId(Long id, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Event> findByIdIn(List<Long> ids);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    Optional<Event> findByIdAndState(Long id, EventState eventState);

    long countByCategoryId(Long categoryId);
}
