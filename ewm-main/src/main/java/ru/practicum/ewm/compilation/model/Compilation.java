package ru.practicum.ewm.compilation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "compilation-events",
        attributeNodes = @NamedAttributeNode(value = "events", subgraph = "event-user-category"),
        subgraphs = @NamedSubgraph(name = "event-user-category",
                attributeNodes = {@NamedAttributeNode("initiator"), @NamedAttributeNode("category")})
)
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Builder.Default
    @Column(name = "pinned")
    private Boolean pinned = false;
    @Size(min = 1, max = 50)
    @Column(name = "title", nullable = false)
    private String title;
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;
}