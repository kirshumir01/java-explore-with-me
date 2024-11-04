package ru.practicum.ewm.comments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "comment-author",
        attributeNodes = @NamedAttributeNode("author"))
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text", nullable = false)
    @Size(min = 1, max = 1000)
    private String text;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Column(name = "event_id", nullable = false)
    private Long event;
    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Builder.Default
    @Column(name = "edits_count", nullable = false)
    private Integer editsCount = 0;
    @Builder.Default
    @Column(name = "editable", nullable = false)
    private Boolean isEditable = true;
    @Column(name = "edited")
    private LocalDateTime edited;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private CommentState state = CommentState.PUBLISHED;
    @Builder.Default
    @Column(name = "parent", nullable = false)
    private Boolean isParent = true;
    @Builder.Default
    @Column(name = "parent_id", nullable = false)
    private Long parentCommentId = null;
}