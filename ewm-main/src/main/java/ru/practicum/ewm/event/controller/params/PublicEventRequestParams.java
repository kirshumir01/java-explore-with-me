package ru.practicum.ewm.event.controller.params;

import com.querydsl.core.BooleanBuilder;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class PublicEventRequestParams {
    @Size(min = 1, max = 7000)
    private String text;
    private Set<Long> categories;
    private Boolean paid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Sorted sort = Sorted.EVENT_DATE;

    public enum Sorted {
        EVENT_DATE, VIEWS
    }

    public BooleanBuilder getBuilder() {
        BooleanBuilder builder = new BooleanBuilder();

        if (text != null && !text.isBlank()) {
            builder.and(QEvent.event.annotation.containsIgnoreCase(text))
                    .or(QEvent.event.description.containsIgnoreCase(text));
        }

        if (categories != null && !categories.isEmpty()) {
            builder.and(QEvent.event.category.id.in(categories));
        }

        if (paid != null) {
            builder.and(QEvent.event.paid.eq(paid));
        }

        if (rangeStart != null && rangeEnd != null) {
            builder.and(QEvent.event.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart == null && rangeEnd == null) {
            builder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }

        builder.and(QEvent.event.state.eq(EventState.PUBLISHED));

        return builder;
    }

    public Sort getSort() {
        if (sort.equals(Sorted.EVENT_DATE)) {
            return Sort.by(Sort.Direction.DESC, "eventDate");
        } else {
            return Sort.by(Sort.Direction.ASC, "views");
        }
    }
}