package com.emobile.springtodo.dto.input;

import com.emobile.springtodo.validation.ValidStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaskFilter {

    @ValidStatus
    String status;
    @Min(message = "Min value = 1", value = 1)
    @Max(message = "Max value = 5", value = 5)
    Integer priority;
    LocalDate created;
    LocalDate due;
}
