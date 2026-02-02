package ru.yandex.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    private String title;
}