package ru.yandex.practicum.request.client.dto;

import lombok.Data;

@Data
public class RequestCountDto {
    private Long eventId;
    private Long count;
}
