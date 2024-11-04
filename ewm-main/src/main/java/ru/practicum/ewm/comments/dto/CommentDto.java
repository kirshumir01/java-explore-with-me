package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.comments.model.CommentState;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto extends CommentShortDto {
    private Long event;
    private Integer editsCount;
    private Boolean isEditable;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String edited;
    private CommentState state;
    private Boolean isParent;
    private List<CommentShortDto> replies = new ArrayList<>();

    public CommentDto(CommentShortDto commentShortDto, Long event, int editsCount, Boolean isEditable,
                      String edited, CommentState state, Boolean isParent, List<CommentShortDto> replies) {
        super(commentShortDto.getId(),
              commentShortDto.getText(),
              commentShortDto.getAuthor(),
              commentShortDto.getCreated(),
              commentShortDto.getParentCommentId()
        );
        this.event = event;
        this.editsCount = editsCount;
        this.isEditable = isEditable;
        this.edited = edited;
        this.state = state;
        this.isParent = isParent;
        this.replies = replies;
    }
}