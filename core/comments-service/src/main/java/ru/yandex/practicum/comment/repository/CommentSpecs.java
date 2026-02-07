package ru.yandex.practicum.comment.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.comment.model.Comment;

public class CommentSpecs {

    public static Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("eventId"), eventId);
    }

    public static Specification<Comment> byAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("authorId"), authorId);
    }

    public static Specification<Comment> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Comment> fetchAll() {
        return (root, query, cb) -> cb.conjunction();
    }
}