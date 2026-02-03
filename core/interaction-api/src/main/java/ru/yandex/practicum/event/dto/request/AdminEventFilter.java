package ru.yandex.practicum.event.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.event.dto.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminEventFilter {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @PositiveOrZero(message = "Параметр 'from' не может быть отрицательным")
    private Integer from = 0;

    @Positive(message = "Параметр 'size' должен быть положительным числом")
    private Integer size = 10;
}
