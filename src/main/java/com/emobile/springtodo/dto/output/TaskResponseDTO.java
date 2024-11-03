package com.emobile.springtodo.dto.output;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaskResponseDTO implements Serializable {

    Long taskId;
    String title;
    String description;
    String status;
    Integer priority;
    LocalDate created;
    LocalDate due;
}
