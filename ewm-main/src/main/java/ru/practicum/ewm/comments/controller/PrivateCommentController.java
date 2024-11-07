package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable long userId,
                                    @RequestParam long eventId,
                                    @RequestParam(required = false) Long parentId,
                                    @Valid @RequestBody NewCommentDto createDto) {
        log.info("Main-service: received PRIVATE request to CREATE comment: {}", createDto);
        createDto.setParentCommentId(parentId);
        CommentDto createdComment = commentService.createComment(userId, eventId, createDto);
        log.info("Main-service: comment created: {}", createdComment);
        return createdComment;
    }

    @GetMapping
    List<CommentShortDto> getAllAuthorCommentsByEvent(@PathVariable long userId,
                                                      @RequestParam long eventId,
                                                      @RequestParam(defaultValue = "0") @Min(0) int from,
                                                      @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size) {
        log.info("Main-service: received PRIVATE request to GET author's comments by eventId id = {}", eventId);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CommentShortDto> comments = commentService.getAllAuthorCommentsByEvent(userId, eventId, pageRequest);
        log.info("Main-service: comments received: {}", comments);
        return comments;
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @Valid @RequestBody UpdateCommentDto updateDto) {
        log.info("Main-service: received PRIVATE request to UPDATE comment by id = {}, updateDTO = {}", commentId, updateDto);
        updateDto.setId(commentId);
        CommentDto updatedComment = commentService.updateCommentById(userId, updateDto);
        log.info("Main-service: comment updated: {}", updatedComment);
        return updatedComment;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.info("Main-service: received PRIVATE request to DELETE comment by id = {}", commentId);
        commentService.deleteComment(userId, commentId);
        log.info("Main-service: comment deleted");
    }
}
