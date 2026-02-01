package ru.yandex.practicum.comment.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.comment.model.Comment;

import jakarta.persistence.criteria.JoinType;

public class CommentSpecs {

    public static Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("event").get("id"), eventId);
    }

    public static Specification<Comment> byAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Comment> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Comment> fetchAll() {
        return (root, query, cb) -> {
            if (Comment.class.equals(query.getResultType())) {
                root.fetch("author", JoinType.LEFT);
                root.fetch("event", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}