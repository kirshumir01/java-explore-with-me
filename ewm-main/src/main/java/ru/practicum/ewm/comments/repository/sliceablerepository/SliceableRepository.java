package ru.practicum.ewm.comments.repository.sliceablerepository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SliceableRepository<T> {
    Slice<T> findAllSliced(EntityPath<T> entityPath, Predicate predicate, Pageable pageable);
}