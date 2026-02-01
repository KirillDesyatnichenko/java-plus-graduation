package ru.yandex.practicum.event.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.event.dto.enums.AdminStateAction;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateEventAdminRequest extends UpdateEventBase {
    private AdminStateAction stateAction;
}
