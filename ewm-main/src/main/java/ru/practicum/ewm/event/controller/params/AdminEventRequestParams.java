package ru.practicum.ewm.event.controller.params;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class AdminEventRequestParams {
    private Set<Long> users = new HashSet<>();
    private Set<EventState> states = new HashSet<>();
    private Set<Long> categories = new HashSet<>();
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    public Predicate getPredicate() {
        BooleanBuilder builder = new BooleanBuilder();

        if (users != null && !users.isEmpty()) {
            builder.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null && !states.isEmpty()) {
            builder.and(QEvent.event.state.in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            builder.and(QEvent.event.category.id.in(categories));
        }

        if (rangeStart != null) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            builder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }

        return builder.getValue();
    }
}
