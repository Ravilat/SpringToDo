package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@RequestMapping("/todo")
@Tag(name = "TASKS", description = "CRUD for tasks")
public interface TaskApiDocs {


    @GetMapping("/task/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получение задачи по ID", description = "")
    TaskResponseDTO getTask(
            @PathVariable
            @NotBlank
            @Parameter(description = "ID задачи")
            String taskId);

    @GetMapping("/tasks")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получение всех задач", description = "Сортировка по дате создания," +
            " возможна фильтрация " +
            " по статусу задачи (NEW, IN_PROGRESS, COMPLETED, DELETED, CANCELLED)," +
            " по приоритету, дате создания и дате дедлайна")
    List<TaskResponseDTO> getAllTasks(
            @Valid
            @ModelAttribute
            TaskFilter taskFilter,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Номер страницы (начиная с 0)")
            @Min(0)
            int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Количество записей на странице")
            @Min(1)
            int size
    );


    @PostMapping("/task")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание задачи", description = "Для создания задачи нужны поля:" +
            "заголовок, описание, приоритет (от 1 до 5), срок выполнения (в днях), " +
            "новая задача создается со статусом NEW")
    TaskResponseDTO createTask(
            @Valid
            @RequestBody
            CreateTaskDto task);


    @PatchMapping("/task/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Изменение задачи", description = "Изменять можно: " +
            "заголовок, описание, статус (NEW, IN_PROGRESS, COMPLETED, DELETED, CANCELLED), " +
            "приоритет (от 1 до 5), срок выполнения (в днях)")
    TaskResponseDTO updateTask(
            @PathVariable
            @Parameter(description = "ID задачи")
            String taskId,
            @Valid
            @RequestBody
            UpdateTaskDTO task);


    @DeleteMapping("/task/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Удаление задачи")
    void deleteTask(
            @PathVariable
            @NotBlank
            @Parameter(description = "ID задачи")
            String taskId);
}
