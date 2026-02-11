package ru.yandex.practicum.event.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.Location;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface EventMapper {

    @Mapping(source = "event.eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    Event fromNewEventDto(NewEventDto dto);

    List<EventShortDto> toEventsShortDto(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserDto(UpdateEventUserRequest dto, @MappingTarget Event entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminDto(UpdateEventAdminRequest dto, @MappingTarget Event entity);

    List<EventFullDto> toEventsFullDto(List<Event> events);

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto dto);

    @Named("mapCategoryIdToCategory")
    default ru.yandex.practicum.category.model.Category mapCategoryIdToCategory(Long id) {
        if (id == null) return null;
        ru.yandex.practicum.category.model.Category category = new ru.yandex.practicum.category.model.Category();
        category.setId(id);
        return category;
    }
}