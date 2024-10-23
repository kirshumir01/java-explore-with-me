package ru.practicum.ewm.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Size(min = 2, max = 250)
    @Column(name = "name", nullable = false)
    private String name;
    @Size(min = 6, max = 254)
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}