package ru.yandex.practicum.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long>, JpaSpecificationExecutor<Compilation> {

    @Query("SELECT c FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @Query("""
            SELECT DISTINCT c
            FROM Compilation c
            LEFT JOIN FETCH c.events e
            LEFT JOIN FETCH e.category
            WHERE c.id IN :ids
            """)
    List<Compilation> findAllWithEventsByIdIn(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT c
            FROM Compilation c
            LEFT JOIN FETCH c.events e
            LEFT JOIN FETCH e.category
            WHERE c.id = :id
            """)
    Compilation findByIdWithEvents(@Param("id") Long id);

    boolean existsByTitle(String title);
}