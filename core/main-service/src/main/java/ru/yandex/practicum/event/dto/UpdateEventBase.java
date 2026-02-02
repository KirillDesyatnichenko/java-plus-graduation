package ru.yandex.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.event.model.Location;

import java.time.LocalDateTime;

@Data
public abstract class UpdateEventBase {
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    protected String annotation;
    protected Long category;
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    protected String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "Дата события должна быть в будущем")
    protected LocalDateTime eventDate;
    @Valid
    protected Location location;
    protected Boolean paid;
    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    protected Integer participantLimit;
    protected Boolean requestModeration;
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    protected String title;
}
