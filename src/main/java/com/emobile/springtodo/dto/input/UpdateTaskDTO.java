package com.emobile.springtodo.dto.input;


import com.emobile.springtodo.validation.ValidStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
public record UpdateTaskDTO(

        @Size(max = 20, message = "title must be shorter than 20 characters")
        String title,
        @Size(message = "max 300 symbols", max = 300)
        String description,
        @ValidStatus
        String status,
        @Min(message = "Min value = 1", value = 1)
        @Max(message = "Max value = 5", value = 5)
        Integer priority,
        @Min(message = "Min value = 1", value = 1)
        Integer dueDate) {
}

