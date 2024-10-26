package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "user-category-location",
        attributeNodes = {
                @NamedAttributeNode("initiator"),
                @NamedAttributeNode("category"),
                @NamedAttributeNode("location")})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "annotation", nullable = false)
    @Size(min = 20, max = 2000)
    private String annotation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "description", nullable = false)
    @Size(min = 20, max = 7000)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    @Builder.Default
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;
    @Builder.Default
    @Column(name = "participant_limit")
    private Integer participantLimit = 0;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Builder.Default
    @Column(name = "request_moderation")
    private Boolean requestModeration = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "eventState")
    private EventState state;
    @Column(name = "title", nullable = false)
    @Size(min = 3, max = 120)
    private String title;
}