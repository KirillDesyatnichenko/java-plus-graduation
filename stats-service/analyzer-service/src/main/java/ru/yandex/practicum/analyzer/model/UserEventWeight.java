package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_event_weights", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEventWeight {
    @EmbeddedId
    private UserEventKey id;

    @Column(name = "weight", nullable = false)
    private double weight;
}