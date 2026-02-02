package ru.yandex.practicum.event.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.event.dto.enums.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PublicEventFilter {
    @Size(min = 1, max = 7000, message = "Текст поиска должен содержать от 1 до 7000 символов")
    private String text;

    private List<Long> categories;

    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;

    private EventSort sort;

    @PositiveOrZero(message = "Параметр 'from' не может быть отрицательным")
    private Integer from = 0;

    @Positive(message = "Параметр 'size' должен быть положительным числом")
    private Integer size = 10;
}