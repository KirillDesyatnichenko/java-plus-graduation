package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.request.dto.ConfirmedRequestCount;
import ru.yandex.practicum.request.dto.RequestCount;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    @Query("""
            SELECT new ru.yandex.practicum.request.dto.ConfirmedRequestCount(r.eventId, COUNT(r))
            FROM Request r
            WHERE r.status = :status
                AND r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<ConfirmedRequestCount> countConfirmedRequestsForEvents(@Param("eventIds") List<Long> eventIds,
                                                                @Param("status") RequestStatus status);

    @Query("""
            SELECT new ru.yandex.practicum.request.dto.RequestCount(r.eventId, COUNT(r))
            FROM Request r
            WHERE r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<RequestCount> countRequestsForEvents(@Param("eventIds") List<Long> eventIds);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}