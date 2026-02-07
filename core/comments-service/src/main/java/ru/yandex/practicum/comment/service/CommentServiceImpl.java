package ru.yandex.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.comment.dto.*;
import ru.yandex.practicum.comment.mapper.CommentMapper;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.comment.repository.CommentRepository;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.client.UserClient;
import ru.yandex.practicum.user.dto.PageParams;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityValidator entityValidator;
    private final UserClient userClient;
    private final EventInfoLookupService eventInfoLookupService;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        UserShortDto author = ensureUserExists(userId);
        eventInfoLookupService.ensureEventExists(eventId);

        Comment comment = Comment.builder()
                .authorId(author.getId())
                .authorName(author.getName())
                .eventId(eventId)
                .text(dto.getText().trim())
                .createdOn(LocalDateTime.now())
                .isDeleted(false)
                .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Нельзя изменять чужой комментарий");
        }
        comment.setText(dto.getText().trim());
        comment.setUpdatedOn(LocalDateTime.now());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Нельзя удалять чужой комментарий");
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        if (comment.isDeleted()) {
            throw new ValidationException("Комментарий удалён");
        }
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, PageParams pageParams) {
        eventInfoLookupService.ensureEventExists(eventId);
        PageRequest pageable = PageRequest.of(pageParams.getPageNumber(), pageParams.getSize());
        return commentRepository.findAllByEventId(eventId, pageable)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getAllComments(Long eventId, Long authorId, Boolean includeDeleted, PageParams pageParams) {
        if (eventId != null) {
            eventInfoLookupService.ensureEventExists(eventId);
        }
        PageRequest pageable = PageRequest.of(pageParams.getPageNumber(), pageParams.getSize());
        return commentRepository.findAllFiltered(eventId, authorId, includeDeleted, pageable)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto adminUpdateComment(Long commentId, String text) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        comment.setText(text.trim());
        comment.setUpdatedOn(LocalDateTime.now());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void adminDeleteComment(Long commentId) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void restoreComment(Long commentId) {
        Comment comment = entityValidator.ensureAndGet(commentRepository, commentId, "Комментарий");
        if (!comment.isDeleted()) {
            throw new ValidationException("Комментарий уже активен");
        }
        comment.setDeleted(false);
        commentRepository.save(comment);
    }

    private UserShortDto ensureUserExists(Long userId) {
        try {
            return userClient.getUserShort(userId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        } catch (Exception ex) {
            throw new ValidationException("User-service недоступен");
        }
    }

}
