package ru.yandex.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.request.dto.ConfirmedRequestCount;
import ru.yandex.practicum.request.dto.RequestCount;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.request.repository.RequestRepository;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final RequestRepository requestRepository;

    @GetMapping("/confirmed")
    public List<ConfirmedRequestCount> getConfirmedCounts(@RequestParam List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return requestRepository.countConfirmedRequestsForEvents(eventIds, RequestStatus.CONFIRMED);
    }

    @GetMapping("/counts")
    public List<RequestCount> getRequestCounts(@RequestParam List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return requestRepository.countRequestsForEvents(eventIds);
    }
}
