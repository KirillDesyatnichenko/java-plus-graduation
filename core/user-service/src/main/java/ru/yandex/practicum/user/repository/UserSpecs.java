package ru.yandex.practicum.user.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.user.model.User;

import java.util.List;

public class UserSpecs {


    public static Specification<User> byId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<User> byIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> root.get("id").in(ids);
    }

    // Оставил для возможного расширения
    public static Specification<User> fetchAll() {
        return (root, query, cb) -> {
            if (User.class.equals(query.getResultType())) {
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}