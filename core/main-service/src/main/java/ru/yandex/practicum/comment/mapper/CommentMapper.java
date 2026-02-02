package ru.yandex.practicum.comment.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "eventId", source = "event.id")
    CommentDto toDto(Comment comment);
}