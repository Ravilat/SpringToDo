package com.emobile.springtodo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Task {

    long id;
    String title;
    String description;
    Status status;
    int priority;
    LocalDate created;
    LocalDate due;

    public Task(String title, String description, Status status, int priority, LocalDate created, LocalDate due) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.created = created;
        this.due = due;
    }
}

