package ru.practicum.ewm.comments.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.sliceablerepository.SliceableRepository;

import java.util.List;
import java.util.Set;

@Repository
public interface CommentRepository extends
        JpaRepository<Comment, Long>,
        SliceableRepository<Comment>,
        QuerydslPredicateExecutor<Comment> {

    @EntityGraph(value = "comment-author")
    @Query("""
            SELECT c FROM Comment c
            WHERE c.author.id = :authorId AND c.event = :eventId
            """)
    List<Comment> findByAuthorIdAndEventId(@Param("authorId") long userId,
                                           @Param("eventId") long eventId);

    @EntityGraph(value = "comment-author")
    Slice<Comment> findAllSliced(EntityPath path, Predicate predicate, Pageable page);

    @Query("""
            SELECT c.id FROM Comment c
            WHERE c.parentCommentId = :parentId
            """)
    Set<Long> findAllIdsByParentCommentId(@Param("parentId") Long parentCommentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.parentCommentId = :parentId
            """)
    List<Comment> findAllByParentCommentId(@Param("parentId") Long parentCommentId);
}
