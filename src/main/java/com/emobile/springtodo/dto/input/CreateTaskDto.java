package com.emobile.springtodo.dto.input;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record CreateTaskDto(
        @NotBlank(message = "Title should not be empty")
        @Size(message = "max 20 symbols", max = 20)
        String title,
        @NotBlank(message = "Description should not be empty")
        @Size(message = "max 300 symbols", max = 300)
        String description,
        @Min(message = "Min value = 1", value = 1)
        @Max(message = "Max value = 5", value = 5)
        @NotNull(message = "Priority cannot be null")
        Integer priority,
        @NotNull(message = "The deadline cannot be zero")
        @Min(1)
        Integer dueDate) {
}
