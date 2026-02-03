package ru.yandex.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.yandex.practicum.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    default List<Comment> findAllByEventId(Long eventId, Pageable pageable) {
        return findAll(
                CommentSpecs.fetchAll()
                        .and(CommentSpecs.byEventId(eventId))
                        .and(CommentSpecs.notDeleted()),
                pageable
        ).getContent();
    }

    default List<Comment> findAllFiltered(Long eventId, Long authorId, Boolean includeDeleted, Pageable pageable) {
        Specification<Comment> spec = CommentSpecs.fetchAll();

        if (eventId != null) {
            spec = spec.and(CommentSpecs.byEventId(eventId));
        }
        if (authorId != null) {
            spec = spec.and(CommentSpecs.byAuthorId(authorId));
        }
        if (includeDeleted == null || !includeDeleted) {
            spec = spec.and(CommentSpecs.notDeleted());
        }

        return findAll(spec, pageable).getContent();
    }
}