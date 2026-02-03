package ru.yandex.practicum.event.dto;

import lombok.Data;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.event.dto.enums.EventState;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    private EventState state;
    private String title;
    private long views;
}

