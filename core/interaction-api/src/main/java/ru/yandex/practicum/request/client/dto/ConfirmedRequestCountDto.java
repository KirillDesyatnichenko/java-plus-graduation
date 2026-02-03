package ru.yandex.practicum.request.client.dto;

import lombok.Data;

@Data
public class ConfirmedRequestCountDto {
    private Long eventId;
    private Long count;
}
