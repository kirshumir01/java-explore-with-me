package ru.practicum.ewm.comments.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.comments.controller.params.AdminCommentRequestParams;
import ru.practicum.ewm.comments.controller.params.PublicCommentRequestParams;
import ru.practicum.ewm.comments.dto.*;

import java.util.List;

public interface CommentService {
    CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto);

    List<CommentShortDto> getAllAuthorCommentsByEvent(long userId, long eventId, PageRequest pageRequest);

    CommentDto updateCommentById(long userId, UpdateCommentDto updateDto);

    void deleteComment(long userId, long commentId);

    List<CommentDto> getAllComments(AdminCommentRequestParams params, PageRequest pageRequest);

    CommentDto getCommentById(long commentId);

    CommentDto updateCommentById(UpdateCommentAdminRequest updateDto);

    List<CommentShortDto> getAllCommentsByEventId(PublicCommentRequestParams params, PageRequest pageRequest, long eventId);
}
