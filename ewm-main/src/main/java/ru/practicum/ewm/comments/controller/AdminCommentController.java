package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.controller.params.AdminCommentRequestParams;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentAdminRequest;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    List<CommentDto> getAllComments(@ModelAttribute(value = "params") AdminCommentRequestParams params,
                                    @RequestParam(defaultValue = "0") @Min(0) int from,
                                    @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size) {
        log.info("Main-service: received ADMIN request to GET all comments");
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CommentDto> comments = commentService.getAllComments(params, pageRequest);
        log.info("Main-service: comments received: {}", comments);
        return comments;
    }

    @GetMapping("/{commentId}")
    CommentDto getComment(@PathVariable long commentId) {
        log.info("Main-service: received ADMIN request to GET comment by id = {}", commentId);
        CommentDto comment = commentService.getCommentById(commentId);
        log.info("Main-service: comment received: {}", comment);
        return comment;
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable long commentId,
                                    @Valid @RequestBody UpdateCommentAdminRequest updateDto) {
        log.info("Main-service: received ADMIN request to UPDATE comment by id = {}, updateDTO = {}", commentId, updateDto);
        updateDto.setId(commentId);
        CommentDto updatedComment = commentService.updateCommentById(updateDto);
        log.info("Main-service: comment updated: {}", updatedComment);
        return updatedComment;
    }
}