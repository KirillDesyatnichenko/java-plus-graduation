package ru.yandex.practicum.event.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.event.dto.enums.UserStateAction;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateEventUserRequest extends UpdateEventBase {
    protected UserStateAction stateAction;
}
