package com.emobile.springtodo.dto.input;

import com.emobile.springtodo.validation.ValidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Builder
public record UpdateTaskDTO(

        @Schema(description = "Заголовок, не более 20 символов")
        @Size(max = 20, message = "title must be shorter than 20 characters")
        String title,

        @Schema(description = "Описание, не более 300 символов")
        @Size(message = "max 300 symbols", max = 300)
        String description,

        @ValidStatus
        @Schema(description = "Статус задачи (NEW, IN_PROGRESS, COMPLETED, DELETED, CANCELLED)")
        String status,

        @Schema(description = "Приоритет, от 1 до 5")
        @Min(message = "Min value = 1", value = 1)
        @Max(message = "Max value = 5", value = 5)
        Integer priority,

        @Schema(description = "изменение срока дедлайна (количество дней от даты создания)")
        @Min(message = "Min value = 1", value = 1)
        Integer dueDate) {
}

