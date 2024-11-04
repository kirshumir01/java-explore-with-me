package ru.practicum.ewm.event.repository.sliceablerepository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.repository.support.Querydsl;

import java.util.List;

public class SliceableRepositoryImpl<T> implements SliceableRepository<T> {
    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    public SliceableRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Slice<T> findAllSliced(final EntityPath<T> entityPath,
                                  final Predicate predicate,
                                  final Pageable pageable) {

        final Querydsl querydsl = new Querydsl(
                entityManager,
                new PathBuilder<>(entityPath.getType(), entityPath.getMetadata())
        );

        final int oneMore = pageable.getPageSize() + 1;

        final JPAQuery<T> query = this.jpaQueryFactory.selectFrom(entityPath)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(oneMore);

        final JPQLQuery<T> querySorted = querydsl.applySorting(pageable.getSort(), query);

        final List<T> entities = querySorted.fetch();

        final int size = entities.size();

        final boolean hasNext = size > pageable.getPageSize();

        if (hasNext) {
            entities.remove(size - 1);
        }

        return new SliceImpl<>(entities, pageable, hasNext);
    }
}