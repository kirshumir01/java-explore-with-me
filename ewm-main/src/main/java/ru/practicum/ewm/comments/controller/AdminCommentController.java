package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.controller.params.AdminCommentRequestParams;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentAdminRequest;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<CommentDto> getAllComments(@ModelAttribute(value = "params") AdminCommentRequestParams params,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        log.info("Main-service: received ADMIN request to GET all comments");
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CommentDto> comments = commentService.getAllComments(params, pageRequest);
        log.info("Main-service: comments received: {}", comments);
        return comments;
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto getComment(@PathVariable long commentId) {
        log.info("Main-service: received ADMIN request to GET comment by id = {}", commentId);
        CommentDto comment = commentService.getCommentById(commentId);
        log.info("Main-service: comment received: {}", comment);
        return comment;
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable long commentId,
                                    @Valid @RequestBody UpdateCommentAdminRequest updateDto) {
        log.info("Main-service: received ADMIN request to UPDATE comment by id = {}, updateDTO = {}", commentId, updateDto);
        updateDto.setId(commentId);
        CommentDto updatedComment = commentService.updateCommentById(updateDto);
        log.info("Main-service: comment updated: {}", updatedComment);
        return updatedComment;
    }
}