package ru.yandex.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.event.dto.enums.EventState;

@Data
@Builder
public class EventInfoDto {
    private Long id;
    private Long initiatorId;
    private EventState state;
    private Integer participantLimit;
    private boolean requestModeration;
}
