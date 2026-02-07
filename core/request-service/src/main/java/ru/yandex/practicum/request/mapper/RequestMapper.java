package ru.yandex.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.request.dto.RequestDto;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    RequestDto toDto(Request request);

    List<RequestDto> toDtoList(List<Request> requests);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "requesterId", source = "requesterId")
    @Mapping(target = "status", source = "status")
    Request toNewEntity(Long eventId, Long requesterId, RequestStatus status);
}