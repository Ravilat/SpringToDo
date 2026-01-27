package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.exception.TaskUpdateException;
import com.emobile.springtodo.mapper.FromCreateDtoToTaskMapper;
import com.emobile.springtodo.mapper.FromTaskToResponseCreateDtoMapper;
import com.emobile.springtodo.mapper.FromUpdateTaskDtoToEntity;
import com.emobile.springtodo.mapper.TaskEntityMapper;
import com.emobile.springtodo.port.input.TaskUseCase;
import com.emobile.springtodo.port.output.TaskOutputManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Service
@Transactional
public class TaskService implements TaskUseCase {

    private final TaskOutputManager outputManager;
    private final FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper;
    private final FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper;
    private final FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity;
    private final TaskEntityMapper taskEntityMapper;
    private final Counter completedTaskCounter;

    public TaskService(TaskOutputManager outputManager,
                       FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper,
                       FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper, FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity, TaskEntityMapper taskEntityMapper,
                       MeterRegistry meterRegistry) {
        this.outputManager = outputManager;
        this.fromCreateDtoToTaskMapper = fromCreateDtoToTaskMapper;
        this.fromTaskToResponseCreateDtoMapper = fromTaskToResponseCreateDtoMapper;
        this.fromUpdateTaskDtoToEntity = fromUpdateTaskDtoToEntity;
        this.taskEntityMapper = taskEntityMapper;
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
        params.put("limit", size);
        params.put("offset", page * size);
        List<Task> tasks = outputManager.getAllTasks(params);

        return tasks.stream()
                .map(fromTaskToResponseCreateDtoMapper::toDTO).toList();
    }

//    @Transactional(readOnly = true)
//    public List<TaskResponseDTO> getAllTasks(TaskFilter taskFilter, int page, int size) {
//
//        Map<String, Object> params = new HashMap<>();
//
//        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE 1=1");
//        if (taskFilter.getStatus() != null) {
//            sql.append(" AND status = :status");
//            params.put("status", taskFilter.getStatus());
//        }
//        if (taskFilter.getPriority() != null) {
//            sql.append(" AND priority = :priority");
//            params.put("priority", taskFilter.getPriority());
//        }
//        if (taskFilter.getCreated() != null) {
//            sql.append(" AND created_at >= :created");
//            params.put("created", Date.valueOf(taskFilter.getCreated()));
//        }
//        if (taskFilter.getDue() != null) {
//            sql.append(" AND due_date <= :due");
//            params.put("due", Date.valueOf(taskFilter.getDue()));
//        }
//        sql.append(" ORDER BY created_at DESC");
//        sql.append(" LIMIT :limit OFFSET :offset");
//
//        params.put("limit", size);
//        params.put("offset", page * size);
//
//        List<Task> tasks = outputManager.getAllTasks(sql.toString(), params);
//
//        return tasks.stream()
//                .map(fromTaskToResponseCreateDtoMapper::toDTO).toList();
//    }

    public TaskResponseDTO create(CreateTaskDto task) {

        Task taskToCreate = fromCreateDtoToTaskMapper.toTask(task);
        Long taskId = outputManager.createTask(taskToCreate);
        taskToCreate.setId(taskId);
        return fromTaskToResponseCreateDtoMapper.toDTO(taskToCreate);

    }


    public TaskResponseDTO update(String taskId, UpdateTaskDTO updateTaskDTO) {

        Task task = getTaskWithoutDto(taskId);
        Map<String, Object> params = new HashMap<>();
        if (updateTaskDTO.title() != null) {
            params.put("title", updateTaskDTO.title());
            task.setTitle(updateTaskDTO.title());
        }
        if (updateTaskDTO.description() != null) {
            params.put("description", updateTaskDTO.description());
            task.setDescription(updateTaskDTO.description());
        }
        if (updateTaskDTO.status() != null) {
            params.put("status", updateTaskDTO.status());
            if ("COMPLETED".equals(updateTaskDTO.status()) && !task.getStatus().equals(Status.COMPLETED)) {
                completedTaskCounter.increment();
            }
            task.setStatus(Status.valueOf(updateTaskDTO.status()));
        }
        if (updateTaskDTO.priority() != null) {
            params.put("priority", updateTaskDTO.priority());
            task.setPriority(updateTaskDTO.priority());
        }
        if (updateTaskDTO.dueDate() != null) {
            LocalDate due = task.getCreated().plusDays(updateTaskDTO.dueDate());
            params.put("due_date", Date.valueOf(due));
            task.setDue(due);
        }
        if (params.isEmpty()) {
            throw new TaskUpdateException("All parameters should not to be null");
        }
        params.put("id", Long.valueOf(taskId));

        //для работы сервиса в Springboot Data Jpa params не нужны, добавляю обновленную сущность
        HibernateEntityTask entityTask = taskEntityMapper.toEntity(task);
        fromUpdateTaskDtoToEntity.updateEntityFromDto(updateTaskDTO, entityTask);


        int update = outputManager.update(params, entityTask);
        if (update == 0) {
            throw new TaskNotFoundException("Task not found");
        }
        return fromTaskToResponseCreateDtoMapper.toDTO(task);

    }

//    public TaskResponseDTO update(String taskId, UpdateTaskDTO updateTaskDTO) {
//
//        Task task;
//
//        try {
//            task = outputManager.getTask(Long.valueOf(taskId));
//        } catch (NumberFormatException e) {
//            throw new TaskNotFoundException("Wrong task id");
//        } catch (EmptyResultDataAccessException e) {
//            throw new TaskNotFoundException("Task not found");
//        }
//
//        Map<String, Object> params = new HashMap<>();
//
//        StringBuilder sql = new StringBuilder("UPDATE tasks SET ");
//
//        if (updateTaskDTO.title() != null) {
//            sql.append("title = :title, ");
//            params.put("title", updateTaskDTO.title());
//            task.setTitle(updateTaskDTO.title());
//        }
//        if (updateTaskDTO.description() != null) {
//            sql.append("description = :description, ");
//            params.put("description", updateTaskDTO.description());
//            task.setDescription(updateTaskDTO.description());
//        }
//        if (updateTaskDTO.status() != null) {
//            sql.append("status = :status, ");
//            params.put("status", updateTaskDTO.status());
//            if ("COMPLETED".equals(updateTaskDTO.status()) && !task.getStatus().equals(Status.COMPLETED)) {
//                completedTaskCounter.increment();
//            }
//            task.setStatus(Status.valueOf(updateTaskDTO.status()));
//        }
//        if (updateTaskDTO.priority() != null) {
//            sql.append("priority = :priority, ");
//            params.put("priority", updateTaskDTO.priority());
//            task.setPriority(updateTaskDTO.priority());
//        }
//        if (updateTaskDTO.dueDate() != null) {
//            sql.append("due_date = :due_date, ");
//            LocalDate due = task.getCreated().plusDays(updateTaskDTO.dueDate());
//            params.put("due_date", Date.valueOf(due));
//            task.setDue(due);
//        }
//
//        if (params.isEmpty()) {
//            throw new TaskUpdateException("All parameters should not to be null");
//        }
//        sql.delete(sql.length() - 2, sql.length());
//        sql.append(" WHERE id = :id");
//        params.put("id", Long.valueOf(taskId));
//
//        int update = outputManager.update(sql.toString(), params);
//        if (update == 0) {
//            throw new TaskNotFoundException("Task not found");
//        }
//        return fromTaskToResponseCreateDtoMapper.toDTO(task);
//
//    }

    public void delete(String taskId) {
        int deleted;
        try {
            deleted = outputManager.delete(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        }
        if (deleted == 0) {
            throw new TaskNotFoundException("Task not found");
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

