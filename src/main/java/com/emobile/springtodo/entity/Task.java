package com.emobile.springtodo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@Entity
@Table(name = "tasks", schema = "public")
@Getter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Column(nullable = false)
    @Setter
    private String title;

    @Setter
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private Status status;

    @Setter
    private Integer priority;

    @Column(name = "created_at", nullable = false, columnDefinition = "date")
    @Setter
    private LocalDate created;

    @Column(name = "due_date", columnDefinition = "date")
    @Setter
    private LocalDate due;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task that = (Task) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
