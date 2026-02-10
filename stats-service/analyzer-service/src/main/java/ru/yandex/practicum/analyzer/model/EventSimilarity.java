package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "event_similarity", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarity {
    @EmbeddedId
    private EventPairKey id;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}