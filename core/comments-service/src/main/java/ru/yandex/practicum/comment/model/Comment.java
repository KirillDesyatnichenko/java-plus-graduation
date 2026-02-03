package ru.yandex.practicum.comment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(min = 1, max = 1000, message = "Комментарий должен содержать от 1 до 1000 символов")
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private boolean isDeleted;
}