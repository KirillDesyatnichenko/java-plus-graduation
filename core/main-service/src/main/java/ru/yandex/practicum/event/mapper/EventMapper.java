package ru.yandex.practicum.event.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(source = "event.eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    Event fromNewEventDto(NewEventDto dto);

    List<EventShortDto> toEventsShortDto(List<Event> events);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserDto(UpdateEventUserRequest dto, @MappingTarget Event entity);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminDto(UpdateEventAdminRequest dto, @MappingTarget Event entity);

    List<EventFullDto> toEventsFullDto(List<Event> events);

    @Named("mapCategoryIdToCategory")
    default ru.yandex.practicum.category.model.Category mapCategoryIdToCategory(Long id) {
        if (id == null) return null;
        ru.yandex.practicum.category.model.Category category = new ru.yandex.practicum.category.model.Category();
        category.setId(id);
        return category;
    }
}
