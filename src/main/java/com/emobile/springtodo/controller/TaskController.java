package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.service.TaskService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class TaskController implements TaskApiDocs {

    private final TaskService taskService;
    private final CacheManager cacheManager;

    public TaskController(TaskService taskService, CacheManager cacheManager) {
        this.taskService = taskService;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskResponseDTO getTask(String taskId) {
        return taskService.getTask(taskId);
    }

    @Override
    @Cacheable(value = "tasksAll", key = "{#status, #priority, #created, #due, #page, #size}")
    public List<TaskResponseDTO> getAllTasks(
            String status,
            Integer priority,
            LocalDate created,
            LocalDate due,
            int page,
            int size
    ) {
        TaskFilter filter = new TaskFilter(status, priority, created, due);
        return taskService.getAllTasks(filter, page, size);
    }

    @Override
    @CacheEvict(value = "tasksAll", allEntries = true)
    public TaskResponseDTO createTask(CreateTaskDto task) {
        TaskResponseDTO taskResponseDTO = taskService.create(task);
        cacheManager.getCache("tasks").put(taskResponseDTO.getTaskId(), taskResponseDTO);
        return taskResponseDTO;
    }

    @Override
    @Caching(
            put = {@CachePut(value = "tasks", key = "#taskId")},
            evict = {@CacheEvict(value = "tasksAll", allEntries = true)}
    )
    public TaskResponseDTO updateTask(String taskId, UpdateTaskDTO task) {
        return taskService.update(taskId, task);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasksAll", allEntries = true),

    })
    public void deleteTask(String taskId) {
        taskService.delete(taskId);
    }

}
