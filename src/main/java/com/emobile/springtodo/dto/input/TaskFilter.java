package com.emobile.springtodo.dto.input;

import com.emobile.springtodo.validation.ValidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

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
public class TaskFilter {

    @Schema(description = "Статус задачи (NEW, IN_PROGRESS, COMPLETED, DELETED, CANCELLED)")
    @ValidStatus
    String status;

    @Schema(description = "Приоритет, от 1 до 5")
    @Min(message = "Min value = 1", value = 1)
    @Max(message = "Max value = 5", value = 5)
    Integer priority;

    @Schema(description = "Дата создания, фильтр по задачам, которые созданы после указанной даты")
    LocalDate created;

    @Schema(description = "Дата дедлайна, фильтр по задачам, в которых дедлайн до указанной даты")
    LocalDate due;
}
