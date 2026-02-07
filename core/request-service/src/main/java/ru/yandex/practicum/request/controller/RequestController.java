package ru.yandex.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.dto.*;
import ru.yandex.practicum.request.service.RequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RequestController {

    private final RequestServiceImpl service;

    @GetMapping("/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return service.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable Long userId,
                                    @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return ResponseEntity.ok(service.cancelRequest(userId, requestId));
    }

    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatuses(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest request) {
        return service.changeRequestStatus(userId, eventId, request);
    }
}