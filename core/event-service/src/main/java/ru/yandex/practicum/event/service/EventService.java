package ru.yandex.practicum.event.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.recommendation.CollectorGrpcClient;
import ru.yandex.practicum.client.recommendation.RecommendationsGrpcClient;
import ru.yandex.practicum.client.recommendation.RecommendedEvent;
import ru.yandex.practicum.client.recommendation.UserActionType;
import ru.yandex.practicum.common.EntityValidator;
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
    private final CollectorGrpcClient collectorGrpcClient;
    private final RecommendationsGrpcClient recommendationsGrpcClient;
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

        if (filter.getSort() != null && filter.getSort() == EventSort.RATING) {
            dtos.sort(Comparator.comparing(EventShortDto::getRating).reversed());
        }

        return dtos;
    }

    public EventFullDto findPublicEventById(long id, Long userId) {
        Event event = findByPublicId(id);

        EventFullDto dto = eventMapper.toEventFullDto(event);
        if (dto != null) {
            if (userId != null) {
                collectUserAction(userId, event.getId(), UserActionType.VIEW);
            }
            enrichFullDto(event, dto, true, null);
        }

        return dto;
    }

    public List<EventShortDto> getRecommendationsForUser(long userId, int size) {
        List<RecommendedEvent> recommendations = recommendationsGrpcClient.getRecommendationsForUser(userId, size);
        if (recommendations == null || recommendations.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = recommendations.stream()
                .map(RecommendedEvent::eventId)
                .toList();

        Map<Long, Event> eventsById = eventRepository.findAllById(eventIds).stream()
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .collect(Collectors.toMap(Event::getId, event -> event));

        List<Event> orderedEvents = new ArrayList<>();
        for (Long eventId : eventIds) {
            Event event = eventsById.get(eventId);
            if (event != null) {
                orderedEvents.add(event);
            }
        }

        List<EventShortDto> dtos = eventMapper.toEventsShortDto(orderedEvents);
        enrichShortDtos(orderedEvents, dtos, false, null);

        Map<Long, Double> scores = recommendations.stream()
                .collect(Collectors.toMap(RecommendedEvent::eventId, RecommendedEvent::score, (a, b) -> a));
        for (EventShortDto dto : dtos) {
            dto.setRating(scores.getOrDefault(dto.getId(), 0.0));
        }

        return dtos;
    }

    public void likeEvent(long userId, long eventId) {
        Event event = findByPublicId(eventId);
        collectUserAction(userId, event.getId(), UserActionType.LIKE);
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
                                 boolean includeRatings,
                                 Map<Long, Long> confirmedCounts) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        Map<Long, Long> eventToInitiator = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getInitiatorId));
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(new ArrayList<>(eventToInitiator.values()));
        Map<Long, Long> counts = confirmedCounts != null ? confirmedCounts
                : externalLookupService.fetchConfirmedCounts(events.stream().map(Event::getId).toList());

        Map<Long, Double> ratings = Map.of();
        if (includeRatings) {
            ratings = fetchRatingsForEvents(events.stream().map(Event::getId).toList());
        }

        for (EventShortDto dto : dtos) {
            Long initiatorId = eventToInitiator.get(dto.getId());
            dto.setInitiator(users.getOrDefault(initiatorId, fallbackUser(initiatorId)));
            dto.setConfirmedRequests(counts.getOrDefault(dto.getId(), 0L));
            if (includeRatings) {
                dto.setRating(ratings.getOrDefault(dto.getId(), 0.0));
            }
        }
    }

    public void enrichShortDtosForEvents(List<Event> events, List<EventShortDto> dtos) {
        enrichShortDtos(events, dtos, true, null);
    }

    private void enrichFullDtos(List<Event> events,
                                List<EventFullDto> dtos,
                                boolean includeRatings,
                                Map<Long, Long> confirmedCounts) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        Map<Long, Long> eventToInitiator = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getInitiatorId));
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(new ArrayList<>(eventToInitiator.values()));
        Map<Long, Long> counts = confirmedCounts != null ? confirmedCounts
                : externalLookupService.fetchConfirmedCounts(events.stream().map(Event::getId).toList());

        Map<Long, Double> ratings = Map.of();
        if (includeRatings) {
            ratings = fetchRatingsForEvents(events.stream().map(Event::getId).toList());
        }

        for (EventFullDto dto : dtos) {
            Long initiatorId = eventToInitiator.get(dto.getId());
            dto.setInitiator(users.getOrDefault(initiatorId, fallbackUser(initiatorId)));
            dto.setConfirmedRequests(counts.getOrDefault(dto.getId(), 0L));
            if (includeRatings) {
                dto.setRating(ratings.getOrDefault(dto.getId(), 0.0));
            }
        }
    }

    private void enrichFullDto(Event event, EventFullDto dto, boolean includeRatings, Long confirmedCount) {
        if (dto == null) {
            return;
        }
        Map<Long, UserShortDto> users = externalLookupService.fetchUserShorts(List.of(event.getInitiatorId()));
        dto.setInitiator(users.getOrDefault(event.getInitiatorId(), fallbackUser(event.getInitiatorId())));

        long confirmed = confirmedCount != null ? confirmedCount
                : externalLookupService.fetchConfirmedCounts(List.of(event.getId()))
                .getOrDefault(event.getId(), 0L);
        dto.setConfirmedRequests(confirmed);

        if (includeRatings) {
            Map<Long, Double> ratings = fetchRatingsForEvents(List.of(event.getId()));
            dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
        }
    }

    private Map<Long, Double> fetchRatingsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        try {
            List<RecommendedEvent> recommendations = recommendationsGrpcClient.getInteractionsCount(
                    eventIds,
                    eventIds.size()
            );
            if (recommendations == null || recommendations.isEmpty()) {
                return Map.of();
            }
            return recommendations.stream()
                    .collect(Collectors.toMap(RecommendedEvent::eventId, RecommendedEvent::score, (a, b) -> a));
        } catch (Exception e) {
            log.error("Не удалось получить рейтинг из Analyzer: {}", e.getMessage());
            return Map.of();
        }
    }

    private void collectUserAction(long userId, long eventId, UserActionType actionType) {
        try {
            collectorGrpcClient.collectUserAction(userId, eventId, actionType);
        } catch (Exception ex) {
            log.error("Не удалось отправить действие пользователя в Collector: {}", ex.getMessage());
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