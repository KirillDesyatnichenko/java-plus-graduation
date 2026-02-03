package ru.yandex.practicum.comment.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "authorName", source = "authorName")
    @Mapping(target = "eventId", source = "eventId")
    CommentDto toDto(Comment comment);
}