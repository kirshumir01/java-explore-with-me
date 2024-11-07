package ru.practicum.ewm.comments.controller.params;

import com.querydsl.core.BooleanBuilder;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.comments.model.CommentState;
import ru.practicum.ewm.comments.model.QComment;

import java.time.LocalDateTime;

@Getter
@Setter
public class PublicCommentRequestParams {
    @Size(min = 1, max = 1000)
    private String text;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Sorted sort = Sorted.DESC;

    public enum Sorted {
        ASC, DESC
    }

    public BooleanBuilder getBuilder() {
        BooleanBuilder builder = new BooleanBuilder();

        if (text != null && !text.isBlank()) {
            builder.and(QComment.comment.text.containsIgnoreCase(text));
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

        builder.and(QComment.comment.state.ne(CommentState.CANCELED));

        builder.and(QComment.comment.isParent);

        return builder;
    }

    public Sort getSort() {
        if (sort.equals(PublicCommentRequestParams.Sorted.DESC)) {
            return Sort.by(Sort.Direction.DESC, "created");
        } else {
            return Sort.by(Sort.Direction.ASC, "created");
        }
    }
}
