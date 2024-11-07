package ru.practicum.ewm.comments.controller.params;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.comments.model.CommentState;
import ru.practicum.ewm.comments.model.QComment;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class AdminCommentRequestParams {
    private Set<Long> users = new HashSet<>();
    private Set<Long> events = new HashSet<>();
    private Set<CommentState> states = new HashSet<>();
    private Boolean isEditable;
    private Boolean isParent;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    public Predicate getPredicate() {
        BooleanBuilder builder = new BooleanBuilder();

        if (users != null && !users.isEmpty()) {
            builder.and(QComment.comment.author.id.in(users));
        }

        if (events != null && !events.isEmpty()) {
            builder.and(QComment.comment.event.in(events));
        }

        if (states != null && !states.isEmpty()) {
            builder.and(QComment.comment.state.in(states));
        }

        if (isEditable != null) {
            builder.and(QComment.comment.isEditable.eq(isEditable));
        }

        if (isParent != null) {
            builder.and(QComment.comment.isParent.eq(isParent));
        }

        if (rangeStart != null) {
            builder.and(QComment.comment.created.after(rangeStart));
        }

        if (rangeEnd != null) {
            builder.and(QComment.comment.created.before(rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            builder.and(QComment.comment.created.after(LocalDateTime.now()));
        }

        return builder.getValue();
    }
}