package ru.yandex.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.yandex.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    default List<User> findAllByIdIn(List<Long> ids, Pageable pageable) {
        return findAll(UserSpecs.fetchAll()
                .and(UserSpecs.byIds(ids)), pageable).getContent();
    }

    default List<User> findAllBy(Pageable pageable) {
        return findAll(UserSpecs.fetchAll(), pageable).getContent();
    }

}