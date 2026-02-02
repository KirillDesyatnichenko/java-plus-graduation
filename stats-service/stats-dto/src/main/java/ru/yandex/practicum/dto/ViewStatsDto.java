package ru.yandex.practicum.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;
}
