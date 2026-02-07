package ru.yandex.practicum.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String reason;
    private String message;
    private String status;
    private LocalDateTime timestamp;
}