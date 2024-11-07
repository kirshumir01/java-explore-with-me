package ru.practicum.ewm.comments.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CommentMapper {
    public Comment toComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        CommentShortDto commentShortDto = CommentMapper.toCommentShortDto(comment);

        String edited;
        if (comment.getEdited() != null) {
            edited = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(comment.getEdited());
        } else {
            edited = null;
        }

        return new  CommentDto(
                commentShortDto,
                comment.getEvent(),
                comment.getEditsCount(),
                comment.getIsEditable(),
                edited,
                comment.getState(),
                comment.getIsParent(),
                new ArrayList<>()
        );
    }

    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).toList();
    }

    public CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(comment.getCreated()))
                .parentCommentId(comment.getParentCommentId() != null ? comment.getParentCommentId() : null)
                .build();
    }

    public List<CommentShortDto> toCommentShortDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentShortDto).toList();
    }
}
