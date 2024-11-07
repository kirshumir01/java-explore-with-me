package ru.practicum.ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.comments.model.CommentState;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentAdminRequest {
    private Long id;
    @Size(min = 1, max = 1000)
    @NotBlank
    private String text;
    @NotNull
    private CommentState state;
}
