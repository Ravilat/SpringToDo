package com.emobile.springtodo.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Builder
public record CreateTaskDto(

        @Schema(description = "Заголовок, не более 20 символов")
        @NotBlank(message = "Title should not be empty")
        @Size(message = "max 20 symbols", max = 20)
        String title,

        @Schema(description = "Описание, не более 300 символов")
        @NotBlank(message = "Description should not be empty")
        @Size(message = "max 300 symbols", max = 300)
        String description,

        @Schema(description = "Приоритет, от 1 до 5")
        @Min(message = "Min value = 1", value = 1)
        @Max(message = "Max value = 5", value = 5)
        @NotNull(message = "Priority cannot be null")
        Integer priority,

        @Schema(description = "изменение срока дедлайна (количество дней от даты создания)")
        @NotNull(message = "The deadline cannot be zero")
        @Min(1)
        Integer dueDate) {
}
