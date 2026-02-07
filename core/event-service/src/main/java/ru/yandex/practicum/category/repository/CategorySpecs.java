package ru.yandex.practicum.category.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.category.model.Category;

public class CategorySpecs {

    public static Specification<Category> byId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Category> byName(String name) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    // Оставил для возможного расширения
    public static Specification<Category> fetchRelations() {
        return (root, query, cb) -> {
            if (Category.class.equals(query.getResultType())) {
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}