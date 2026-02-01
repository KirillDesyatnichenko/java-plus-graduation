package ru.yandex.practicum.request.repository;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;


public class RequestSpecs {

    public static Specification<Request> byEvent(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("event").get("id"), eventId);
    }

    public static Specification<Request> byRequester(Long requesterId) {
        return (root, query, cb) -> cb.equal(root.get("requester").get("id"), requesterId);
    }

    public static Specification<Request> byStatus(RequestStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Request> byStatuses(Iterable<RequestStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Request> fetchRequesterAndEvent() {
        return (root, query, cb) -> {
            if (Request.class.equals(query.getResultType())) {
                root.fetch("requester", JoinType.LEFT);
                root.fetch("event", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Request> byEventAndIds(Long eventId, Iterable<Long> ids) {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("event").get("id"), eventId),
                        root.get("id").in(ids)
                );
    }
}