package ru.yandex.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.request.dto.RequestDto;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    RequestDto toDto(Request request);

    List<RequestDto> toDtoList(List<Request> requests);

    @Mapping(target = "id", ignore = true)
    Request toNewEntity(Event event, User requester, RequestStatus status);

}