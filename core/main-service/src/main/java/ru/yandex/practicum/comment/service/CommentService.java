package ru.yandex.practicum.comment.service;

import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.NewCommentDto;
import ru.yandex.practicum.comment.dto.UpdateCommentDto;
import ru.yandex.practicum.user.dto.PageParams;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    CommentDto getCommentById(Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId, PageParams pageParams);

    // Admin методы
    List<CommentDto> getAllComments(Long eventId, Long authorId, Boolean includeDeleted, PageParams pageParams);

    CommentDto adminUpdateComment(Long commentId, String text);

    void adminDeleteComment(Long commentId);

    void restoreComment(Long commentId);
}