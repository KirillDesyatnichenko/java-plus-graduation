package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.UserEventKey;
import ru.yandex.practicum.analyzer.model.UserEventWeight;

import java.util.Collection;
import java.util.List;

public interface UserEventWeightRepository extends JpaRepository<UserEventWeight, UserEventKey> {

    @Query("select u from UserEventWeight u where u.id.userId = :userId")
    List<UserEventWeight> findAllByUserId(@Param("userId") long userId);

    @Query("select u.id.eventId as eventId, sum(u.weight) as score " +
            "from UserEventWeight u where u.id.eventId in :eventIds group by u.id.eventId")
    List<EventScoreView> sumWeightsByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("select u.id.eventId as eventId, sum(u.weight) as score from UserEventWeight u group by u.id.eventId")
    List<EventScoreView> sumWeightsForAll();
}