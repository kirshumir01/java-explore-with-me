package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.controller.params.PublicCommentRequestParams;
import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    List<CommentShortDto> getAllComments(@ModelAttribute(value = "params") PublicCommentRequestParams params,
                                         @PathVariable long eventId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        log.info("Main-service: received PUBLIC request to GET all comments by event id = {}", eventId);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CommentShortDto> comments = commentService.getAllCommentsByEventId(params, pageRequest, eventId);
        log.info("Main-service: comment received: {}", comments);
        return comments;
    }
}
