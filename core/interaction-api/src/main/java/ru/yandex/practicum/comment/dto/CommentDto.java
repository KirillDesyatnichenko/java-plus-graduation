package ru.yandex.practicum.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    private Long authorId;
    private Long eventId;
    private String authorName;
    private String text;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}