package ru.yandex.practicum.event.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.dto.enums.AdminStateAction;
import ru.yandex.practicum.event.dto.enums.EventSort;
import ru.yandex.practicum.event.dto.enums.UserStateAction;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.dto.request.UserEventsQuery;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.dto.enums.EventState;
import ru.yandex.practicum.exception.ExistException;
import ru.yandex.practicum.exception.InvalidDateRangeException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.event.service.ExternalLookupService;
import ru.yandex.practicum.user.client.UserClient;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final HttpServletRequest request;
    private final EntityValidator entityValidator;
    private final UserClient userClient;
    private final ExternalLookupService externalLookupService;

    public List<EventShortDto> findEvents(UserEventsQuery query) {
        List<Event> events = eventRepository.findByInitiatorId(
                query.userId(),
                PageRequest.of(query.from() / query.size(), query.size())
        );

        List<EventShortDto> dtos = eventMapper.toEventsShortDto(events);
        enrichShortDtos(events, dtos, true, null);
        return dtos;
    }

    @Transactional
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        UserShortDto owner = getUserShortOrThrow(userId);

        if (eventDto.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (eventDto.getEventDate().isBefore(now.plusHours(2))) {
                throw new ValidationException("Event date must be at least 2 hours in the future");
            }
        }

        Event event = eventMapper.fromNewEventDto(eventDto);
        event.setInitiatorId(owner.getId());
        event.setState(EventState.PENDING);

        Event savedItem = eventRepository.save(event);

        EventFullDto dto = eventMapper.toEventFullDto(savedItem);
        enrichFullDto(savedItem, dto, true, null);
        return dto;
    }

    public EventFullDto findUserEventById(long userId, long eventId) {
        Event event = findByIdAndUser(eventId, userId);
        EventFullDto dto = eventMapper.toEventFullDto(event);
        enrichFullDto(event, dto, true, null);
        return dto;
    }

    private Event findByPublicId(long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event findByIdAndUser(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Владелец с ID " + userId + " или ивент с ID " + eventId + " не найдены"));
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = findByIdAndUser(eventId, userId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ExistException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (updateRequest.getEventDate().isBefore(now.plusHours(2))) {
                throw new ValidationException("Event date must be at least 2 hours in the future");
            }
        }

        eventMapper.updateEventFromUserDto(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            if (event.getState().equals(EventState.CANCELED) &&
                    updateRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (event.getState().equals(EventState.PENDING) &&
                    updateRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(saved);
        enrichFullDto(saved, dto, false, null);
        return dto;
    }

    public List<EventShortDto> searchPublicEvents(PublicEventFilter filter) {
        if (filter.getRangeStart() != null && filter.getRangeEnd() != null) {
            if (filter.getRangeStart().isAfter(filter.getRangeEnd())) {
                throw new InvalidDateRangeException("Дата начала не может быть позже даты окончания.");
            }
        }

        List<Event> events = eventRepository.searchEventsByPublic(filter);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedCounts = externalLookupService.fetchConfirmedCounts(eventIds);
        Map<Long, Long> requestCounts = externalLookupService.fetchRequestCounts(eventIds);

        if (Boolean.TRUE.equals(filter.getOnlyAvailable())) {
            events = events.stream()
                    .filter(event -> isAvailable(event, requestCounts))
                    .toList();
        }

        List<EventShortDto> dtos = eventMapper.toEventsShortDto(events);
        enrichShortDtos(events, dtos, true, confirmedCounts);

        saveHit();

        if (filter.getSort() != null && filter.getSort() == EventSort.VIEWS) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return dtos;
    }

    public EventFullDto findPublicEventById(long id) {
        Event event = findByPublicId(id);

        EventFullDto dto = eventMapper.toEventFullDto(event);
        if (dto != null) {
            String uri = request.getRequestURI();
            saveHit(uri);
            enrichFullDto(event, dto, true, null);
        }

        return dto;
    }

    public List<EventFullDto> searchEventsByAdmin(AdminEventFilter filter) {
        List<Event> events = eventRepository.searchEventsByAdmin(filter);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedCounts = externalLookupService.fetchConfirmedCounts(eventIds);

        List<EventFullDto> dtos = eventMapper.toEventsFullDto(events);
        enrichFullDtos(events, dtos, true, confirmedCounts);
        return dtos;
    }

    @Transactional
    public EventFullDto moderateEvent(Long eventId, UpdateEventAdminRequest adminRequest) {
        Event event = entityValidator.ensureAndGet(eventRepository, eventId, "Event");

        if (adminRequest.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (adminRequest.getEventDate().isBefore(now.plusHours(1))) {
                throw new ExistException("Event date must be at least one hour in the future to publish.");
            }
        }

        eventMapper.updateEventFromAdminDto(adminRequest, event);

        if (adminRequest.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING)) {
                if (adminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT))
                    event.setState(EventState.PUBLISHED);
                if (adminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT))
                    event.setState(EventState.CANCELED);
            } else {
                throw new ExistException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
        }

        Event saved = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(saved);
        enrichFullDto(saved, dto, false, null);
        return dto;
    }

    public EventInfoDto getEventInfo(Long eventId) {
        Event event = entityValidator.ensureAndGet(eventRepository, eventId, "Event");
        return EventInfoDto.builder()
                .id(event.getId())
                .initiatorId(event.getInitiatorId())
                .state(event.getState())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .build();
    }

    private boolean isAvailable(Event event, Map<Long, Long> confirmedCounts) {
        Integer limit = event.getParticipantLimit();
        if (limit == null || limit == 0) {
            return true;
        }
        long confirmed = confirmedCounts.getOrDefault(event.getId(), 0L);
        return confirmed < limit;
    }

    private void enrichShortDtos(List<Event> events,
                                 List<EventShortDto> dtos,
                                 boolean includeViews,
                                 Map<Long, Long> confirmedCounts) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        Map<Long, Long> eventToInitiator = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getInitiatorId));
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(new ArrayList<>(eventToInitiator.values()));
        Map<Long, Long> counts = confirmedCounts != null ? confirmedCounts
                : externalLookupService.fetchConfirmedCounts(events.stream().map(Event::getId).toList());

        Map<String, Long> hits = Map.of();
        if (includeViews) {
            List<String> uris = dtos.stream().map(d -> "/events/" + d.getId()).toList();
            hits = fetchHitsForUris(uris);
        }

        for (EventShortDto dto : dtos) {
            Long initiatorId = eventToInitiator.get(dto.getId());
            dto.setInitiator(users.getOrDefault(initiatorId, fallbackUser(initiatorId)));
            dto.setConfirmedRequests(counts.getOrDefault(dto.getId(), 0L));
            if (includeViews) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }
    }

    public void enrichShortDtosForEvents(List<Event> events, List<EventShortDto> dtos) {
        enrichShortDtos(events, dtos, true, null);
    }

    private void enrichFullDtos(List<Event> events,
                                List<EventFullDto> dtos,
                                boolean includeViews,
                                Map<Long, Long> confirmedCounts) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        Map<Long, Long> eventToInitiator = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getInitiatorId));
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(new ArrayList<>(eventToInitiator.values()));
        Map<Long, Long> counts = confirmedCounts != null ? confirmedCounts
                : externalLookupService.fetchConfirmedCounts(events.stream().map(Event::getId).toList());

        Map<String, Long> hits = Map.of();
        if (includeViews) {
            List<String> uris = dtos.stream().map(d -> "/events/" + d.getId()).toList();
            hits = fetchHitsForUris(uris);
        }

        for (EventFullDto dto : dtos) {
            Long initiatorId = eventToInitiator.get(dto.getId());
            dto.setInitiator(users.getOrDefault(initiatorId, fallbackUser(initiatorId)));
            dto.setConfirmedRequests(counts.getOrDefault(dto.getId(), 0L));
            if (includeViews) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }
    }

    private void enrichFullDto(Event event, EventFullDto dto, boolean includeViews, Long confirmedCount) {
        if (dto == null) {
            return;
        }
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(List.of(event.getInitiatorId()));
        dto.setInitiator(users.getOrDefault(event.getInitiatorId(), fallbackUser(event.getInitiatorId())));

        long confirmed = confirmedCount != null ? confirmedCount
                : externalLookupService.fetchConfirmedCounts(List.of(event.getId()))
                .getOrDefault(event.getId(), 0L);
        dto.setConfirmedRequests(confirmed);

        if (includeViews) {
            Map<String, Long> hits = fetchHitsForUris(List.of("/events/" + dto.getId()));
            dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
        }
    }

    private void saveHit() {
        saveHit(request.getRequestURI());
    }

    private void saveHit(String uri) {
        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app("event-service")
                    .uri(uri)
                    .ip(resolveClientIp())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.saveHit(hitDto);
        } catch (Exception e) {
            log.error("Не удалось отправить информацию о просмотре в сервис статистики: {}", e.getMessage());
        }
    }

    private String resolveClientIp() {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private Map<String, Long> fetchHitsForUris(List<String> uris) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
            if (stats == null || stats.isEmpty()) return Map.of();
            return stats.stream().collect(Collectors.toMap(
                    ViewStatsDto::getUri, v -> v.getHits() == null ? 0L : v.getHits()));
        } catch (Exception e) {
            log.error("Не удалось получить просмотры из сервиса статистики: {}", e.getMessage());
            return Map.of();
        }
    }

    private UserShortDto getUserShortOrThrow(Long userId) {
        try {
            return userClient.getUserShort(userId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        } catch (Exception ex) {
            throw new ValidationException("User-service недоступен");
        }
    }

    private UserShortDto fallbackUser(Long userId) {
        UserShortDto dto = new UserShortDto();
        dto.setId(userId == null ? 0L : userId);
        dto.setName("unknown");
        return dto;
    }
}
