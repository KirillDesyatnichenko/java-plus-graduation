package ru.yandex.practicum.controller;


import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.service.StatsService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public ResponseEntity<EndpointHitDto> saveHit(@Valid @RequestBody EndpointHitDto hitDto,
                                                  UriComponentsBuilder uriComponentsBuilder) {
        EndpointHitDto savedHit = statsService.saveHit(hitDto);

        URI location = uriComponentsBuilder
                .path("/hit/{id}")
                .buildAndExpand(savedHit.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(savedHit);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false, defaultValue = "") List<String> uris,
            @RequestParam(required = false, defaultValue = "false") boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }
}
