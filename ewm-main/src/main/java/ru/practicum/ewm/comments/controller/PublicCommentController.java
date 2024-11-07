package ru.practicum.ewm.comments.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.controller.params.PublicCommentRequestParams;
import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/{eventId}")
    List<CommentShortDto> getAllComments(@ModelAttribute(value = "params") PublicCommentRequestParams params,
                                         @PathVariable long eventId,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size) {
        log.info("Main-service: received PUBLIC request to GET all comments by event id = {}", eventId);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CommentShortDto> comments = commentService.getAllCommentsByEventId(params, pageRequest, eventId);
        log.info("Main-service: comment received: {}", comments);
        return comments;
    }
}
