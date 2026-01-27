package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.exception.TaskUpdateException;
import com.emobile.springtodo.mapper.FromCreateDtoToTaskMapper;
import com.emobile.springtodo.mapper.FromTaskToResponseCreateDtoMapper;
import com.emobile.springtodo.mapper.FromUpdateTaskDtoToEntity;
import com.emobile.springtodo.port.output.TaskOutputManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Service
@Transactional
public class TaskService {

    private final TaskOutputManager outputManager;
    private final FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper;
    private final FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper;
    private final FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity;
    private final Counter completedTaskCounter;

    public TaskService(TaskOutputManager outputManager,
                       FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper,
                       FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper,
                       FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity,
                       MeterRegistry meterRegistry) {
        this.outputManager = outputManager;
        this.fromCreateDtoToTaskMapper = fromCreateDtoToTaskMapper;
        this.fromTaskToResponseCreateDtoMapper = fromTaskToResponseCreateDtoMapper;
        this.fromUpdateTaskDtoToEntity = fromUpdateTaskDtoToEntity;
        this.completedTaskCounter = meterRegistry.counter("completed_tasks");
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTask(String taskId) {
        Task task = getTaskWithoutDto(taskId);
        return fromTaskToResponseCreateDtoMapper.toDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(TaskFilter taskFilter, int page, int size) {

        Map<String, Object> params = new HashMap<>();
        if (taskFilter.getStatus() != null) {
            params.put("status", taskFilter.getStatus());
        }
        if (taskFilter.getPriority() != null) {
            params.put("priority", taskFilter.getPriority());
        }
        if (taskFilter.getCreated() != null) {
            params.put("created", Date.valueOf(taskFilter.getCreated()));
        }
        if (taskFilter.getDue() != null) {
            params.put("due", Date.valueOf(taskFilter.getDue()));
        }
        params.put("size", size);
        params.put("offset", page * size);
        List<Task> tasks = outputManager.getAllTasks(params);

        return tasks.stream()
                .map(fromTaskToResponseCreateDtoMapper::toDTO).toList();
    }

    public TaskResponseDTO create(CreateTaskDto task) {

        Task taskToCreate = fromCreateDtoToTaskMapper.toTask(task);
        Long taskId = outputManager.createTask(taskToCreate);
        taskToCreate.setId(taskId);
        return fromTaskToResponseCreateDtoMapper.toDTO(taskToCreate);

    }

    public TaskResponseDTO update(String taskId, UpdateTaskDTO updateTaskDTO) {

        Task task = getTaskWithoutDto(taskId);
        if (updateTaskDTO.status() != null) {
            if ("COMPLETED".equals(updateTaskDTO.status()) && !task.getStatus().equals(Status.COMPLETED)) {
                completedTaskCounter.increment();
            }
        }

        if (updateTaskDTO.title() == null
                && updateTaskDTO.description() == null
                && updateTaskDTO.status() == null
                && updateTaskDTO.priority() == null
                && updateTaskDTO.dueDate() == null) {
            throw new TaskUpdateException("All parameters should not to be null");
        }

        fromUpdateTaskDtoToEntity.updateEntityFromDto(updateTaskDTO, task);

        int update = outputManager.update(task);
        if (update == 0) {
            throw new TaskNotFoundException("Task not found");
        }
        return fromTaskToResponseCreateDtoMapper.toDTO(task);
    }

    public void delete(String taskId) {
        int deleted;
        try {
            deleted = outputManager.delete(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        }
        if (deleted == 0) {
            throw new TaskNotFoundException("Task was not deleted");
        }
    }

    private Task getTaskWithoutDto(String taskId) {
        Task task;
        try {
            task = outputManager.getTask(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task not found");
        }
        if (task == null) {
            throw new TaskNotFoundException("Task not found");
        }
        return task;
    }

}

