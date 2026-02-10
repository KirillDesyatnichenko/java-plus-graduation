package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.EventPairKey;
import ru.yandex.practicum.analyzer.model.EventSimilarity;

import java.util.Collection;
import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventPairKey> {

    @Query("select e from EventSimilarity e where e.id.eventA = :eventId or e.id.eventB = :eventId")
    List<EventSimilarity> findAllByEvent(@Param("eventId") long eventId);

    @Query("select e from EventSimilarity e where e.id.eventA in :eventIds or e.id.eventB in :eventIds")
    List<EventSimilarity> findAllByAnyEvent(@Param("eventIds") Collection<Long> eventIds);
}