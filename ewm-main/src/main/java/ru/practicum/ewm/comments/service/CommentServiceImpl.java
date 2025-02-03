package ru.practicum.ewm.comments.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.controller.params.AdminCommentRequestParams;
import ru.practicum.ewm.comments.controller.params.PublicCommentRequestParams;
import ru.practicum.ewm.comments.dto.*;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentState;
import ru.practicum.ewm.comments.model.QComment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.EventDateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto) {
        Long parentCommentId = newCommentDto.getParentCommentId();
        Comment comment = CommentMapper.toComment(newCommentDto);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (event.getInitiator().getId() == userId && parentCommentId == null) {
            throw new ConflictException("Author can't leave comment to own event");
        }

        if (parentCommentId != null) {
            if (!commentRepository.existsById(parentCommentId)) {
                throw new NotFoundException(String.format("Comment with id = %d not found", parentCommentId));
            }
            comment.setParentCommentId(parentCommentId);
            comment.setIsParent(false);
        }

        comment.setAuthor(author);
        comment.setEvent(event.getId());

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getAllAuthorCommentsByEvent(long userId, long eventId, PageRequest pageRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("Event with id = %d not found", eventId));
        }

        List<Comment> comments = commentRepository.findByAuthorIdAndEventId(userId, eventId);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return CommentMapper.toCommentShortDtoList(comments);
    }

    @Override
    @Transactional
    public CommentDto updateCommentById(long userId, UpdateCommentDto updateDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        Comment commentToUpdate = commentRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id = %d not found", updateDto.getId())));

        if (commentToUpdate.getIsEditable().equals(false)) {
            throw new ConflictException("Non-editable comment couldn't be edited");
        }

        if (commentToUpdate.getState().equals(CommentState.CANCELED)) {
            throw new ConflictException("Cancelled comment comment couldn't be edited");
        }

        if (commentToUpdate.getEditsCount() >= 2) {
            throw new ConflictException("Comment couldn't be edited more than two times");
        }

        if (commentToUpdate.getCreated().plusHours(2).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Comment couldn't be updated after more than 2 hours of publish date");
        }

        if (!commentToUpdate.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Comment couldn't be updated by other user except author");
        }

        if (updateDto.getText() != null && !updateDto.getText().isBlank()) {
            commentToUpdate.setText(updateDto.getText());
        }

        int editsCount = commentToUpdate.getEditsCount() + 1;
        commentToUpdate.setEditsCount(editsCount);
        commentToUpdate.setEdited(LocalDateTime.now());
        commentToUpdate.setState(CommentState.EDITED);

        Comment updatedComment = commentRepository.save(commentToUpdate);

        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(long userId, long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id = %d not found", commentId)));

        if (comment.getAuthor().getId() != userId) {
            throw new ConflictException("Comment couldn't be deleted by other user except author");
        }

        Set<Long> replyIds = new HashSet<>();

        if (comment.getParentCommentId() == null) {
            replyIds = commentRepository.findAllIdsByParentCommentId(commentId);
        }
        replyIds.add(commentId);
        commentRepository.deleteAllById(replyIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllComments(AdminCommentRequestParams params, PageRequest pageRequest) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            if (params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new EventDateException("Start comment publishing date can't be earlier than start date");
            }
        }

        List<Comment> comments;

        if (Objects.nonNull(params.getPredicate())) {
            comments = commentRepository.findAllSliced(QComment.comment, params.getPredicate(), pageRequest).getContent();
        } else {
            comments = commentRepository.findAllSliced(null, null, pageRequest).getContent();
        }

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return buildCommentTree(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id = %d not found", commentId)));

        if (comment.getParentCommentId() == null) {
            List<Comment> replies = commentRepository.findAllByParentCommentId(commentId);
            replies.add(comment);
            return buildCommentTree(replies).getFirst();
        }

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentById(UpdateCommentAdminRequest updateDto) {
        Comment commentToUpdate = commentRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id = %d not found", updateDto.getId())));

        List<String> states = Arrays.stream(CommentState.values()).map(CommentState::name).toList();

        if (!states.contains(updateDto.getState().name())) {
            throw new ConflictException(String.format("State '%s' couldn't be set to comment", updateDto.getState().name()));
        }

        if (updateDto.getText() != null && !updateDto.getText().isBlank()) {
            commentToUpdate.setEdited(LocalDateTime.now());
            commentToUpdate.setText(updateDto.getText());
        }

        commentToUpdate.setState(updateDto.getState());

        if (updateDto.getState().equals(CommentState.CANCELED)) {
            commentToUpdate.setIsEditable(false);
        }

        Comment updatedComment = commentRepository.save(commentToUpdate);

        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getAllCommentsByEventId(PublicCommentRequestParams params,
                                                         PageRequest pageRequest,
                                                         long eventId) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            if (params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new EventDateException("Start comment publishing date can't be earlier than start date");
            }
        }

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("Event with id = %d not found", eventId));
        }

        BooleanBuilder builder = params.getBuilder();
        builder.and(QComment.comment.event.eq(eventId));

        List<Comment> comments = commentRepository
                .findAllSliced(
                        QComment.comment,
                        builder.getValue(),
                        pageRequest.withSort(Sort.by(Sort.Direction.DESC, "created"))
                ).getContent();

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return CommentMapper.toCommentShortDtoList(comments);
    }

    private List<CommentDto> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentDto> parentCommentsMap = comments.stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toMap(CommentDto::getId, Function.identity()));

        Map<Long, List<CommentShortDto>> repliesMap = comments.stream()
                .filter(comment -> comment.getParentCommentId() != null)
                .map(CommentMapper::toCommentShortDto)
                .collect(Collectors.groupingBy(CommentShortDto::getParentCommentId));

        for (CommentDto rootComment : parentCommentsMap.values()) {
            List<CommentShortDto> replies = repliesMap.get(rootComment.getId());
            if (replies != null) {
                replies.sort(Comparator.comparing(CommentShortDto::getCreated).reversed());
                rootComment.setReplies(replies);
            }
        }

        List<CommentDto> sortedRootComments = new ArrayList<>(parentCommentsMap.values());
        sortedRootComments.sort(Comparator.comparing(CommentDto::getCreated).reversed());

        return sortedRootComments;
    }
}