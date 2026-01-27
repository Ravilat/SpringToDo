package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.port.input.TaskUseCase;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@RestController
public class TaskController implements TaskApiDocs {

    private final TaskUseCase entityUseCase;
    private final CacheManager cacheManager;

    public TaskController(com.emobile.springtodo.service.TaskService entityUseCase, CacheManager cacheManager) {
        this.entityUseCase = entityUseCase;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskResponseDTO getTask(String taskId) {
        return entityUseCase.getTask(taskId);
    }

    @Override
    @Cacheable(value = "tasksAll", key = "{#status, #priority, #created, #due, #page, #size}")
    public List<TaskResponseDTO> getAllTasks(
            TaskFilter taskFilter,
            int page,
            int size
    ) {
        return entityUseCase.getAllTasks(taskFilter, page, size);
    }

    @Override
    @CacheEvict(value = "tasksAll", allEntries = true)
    public TaskResponseDTO createTask(CreateTaskDto task) {
        TaskResponseDTO taskResponseDTO = entityUseCase.create(task);
        cacheManager.getCache("tasks").put(taskResponseDTO.getTaskId(), taskResponseDTO);
        return taskResponseDTO;
    }

    @Override
    @Caching(
            put = {@CachePut(value = "tasks", key = "#taskId")},
            evict = {@CacheEvict(value = "tasksAll", allEntries = true)}
    )
    public TaskResponseDTO updateTask(String taskId, UpdateTaskDTO task) {
        return entityUseCase.update(taskId, task);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasksAll", allEntries = true),

    })
    public void deleteTask(String taskId) {
        entityUseCase.delete(taskId);
    }

}
