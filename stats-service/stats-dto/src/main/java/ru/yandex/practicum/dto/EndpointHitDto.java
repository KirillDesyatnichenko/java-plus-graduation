package ru.yandex.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EndpointHitDto {
    private long id;

    @NotBlank(message = "App cannot be blanc")
    private String app;
    @NotBlank(message = "URI cannot be blanc")
    private String uri;
    @NotBlank(message = "IP cannot be blanc")
    private String ip;
    @NotNull(message = "Timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
