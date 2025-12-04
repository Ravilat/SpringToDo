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
import com.emobile.springtodo.repository.TaskRepo;
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

@Service
@Transactional
public class TaskService {

    private final TaskRepo taskRepo;
    private final FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper;
    private final FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper;
    private final MeterRegistry meterRegistry;
    private final Counter completedTaskCounter;

    public TaskService(TaskRepo taskRepo, FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper, FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper, MeterRegistry meterRegistry) {
        this.taskRepo = taskRepo;
        this.fromCreateDtoToTaskMapper = fromCreateDtoToTaskMapper;
        this.fromTaskToResponseCreateDtoMapper = fromTaskToResponseCreateDtoMapper;
        this.meterRegistry = meterRegistry;
        this.completedTaskCounter = meterRegistry.counter("completed_tasks");
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTask(String taskId) {
        Task task;
        try {
            task = taskRepo.getTask(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task not found");
        }
        return fromTaskToResponseCreateDtoMapper.toDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(TaskFilter taskFilter, int page, int size) {

        Map<String, Object> params = new HashMap<>();

        StringBuilder SQL = new StringBuilder("SELECT * FROM tasks WHERE 1=1");
        if (taskFilter.getStatus() != null) {
            SQL.append(" AND status = :status");
            params.put("status", taskFilter.getStatus());
        }
        if (taskFilter.getPriority() != null) {
            SQL.append(" AND priority = :priority");
            params.put("priority", taskFilter.getPriority());
        }
        if (taskFilter.getCreated() != null) {
            SQL.append(" AND created_at >= :created");
            params.put("created", Date.valueOf(taskFilter.getCreated()));
        }
        if (taskFilter.getDue() != null) {
            SQL.append(" AND due_date <= :due");
            params.put("due", Date.valueOf(taskFilter.getDue()));
        }
        SQL.append(" ORDER BY created_at DESC");
        SQL.append(" LIMIT :limit OFFSET :offset");

        params.put("limit", size);
        params.put("offset", page * size);

        List<Task> tasks = taskRepo.getAllTasks(SQL.toString(), params);

        return tasks.stream()
                .map(fromTaskToResponseCreateDtoMapper::toDTO).toList();
    }

    public TaskResponseDTO create(CreateTaskDto task) {

        Task taskToCreate = fromCreateDtoToTaskMapper.toTask(task);
        Long taskId = taskRepo.createTask(taskToCreate);
        taskToCreate.setId(taskId);
        return fromTaskToResponseCreateDtoMapper.toDTO(taskToCreate);

    }


    public TaskResponseDTO update(String taskId, UpdateTaskDTO updateTaskDTO) {

        Task task;

        try {
            task = taskRepo.getTask(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task not found");
        }

        Map<String, Object> params = new HashMap<>();

        StringBuilder SQL = new StringBuilder("UPDATE tasks SET ");

        if (updateTaskDTO.title() != null) {
            SQL.append("title = :title, ");
            params.put("title", updateTaskDTO.title());
            task.setTitle(updateTaskDTO.title());
        }
        if (updateTaskDTO.description() != null) {
            SQL.append("description = :description, ");
            params.put("description", updateTaskDTO.description());
            task.setDescription(updateTaskDTO.description());
        }
        if (updateTaskDTO.status() != null) {
            SQL.append("status = :status, ");
            params.put("status", updateTaskDTO.status());
            task.setStatus(Status.valueOf(updateTaskDTO.status()));
            if ("COMPLETED".equals(updateTaskDTO.status()) && !task.getStatus().equals(Status.COMPLETED)) {
                completedTaskCounter.increment();
            }
        }
        if (updateTaskDTO.priority() != null) {
            SQL.append("priority = :priority, ");
            params.put("priority", updateTaskDTO.priority());
            task.setPriority(updateTaskDTO.priority());
        }
        if (updateTaskDTO.dueDate() != null) {
            SQL.append("due_date = :due_date, ");
            LocalDate due = task.getCreated().plusDays(updateTaskDTO.dueDate());
            params.put("due_date", Date.valueOf(due));
            task.setDue(due);
        }

        if (params.isEmpty()) {
            throw new TaskUpdateException("All parameters should not to be null");
        }
        SQL.delete(SQL.length() - 2, SQL.length());
        SQL.append(" WHERE id = :id");
        params.put("id", Long.valueOf(taskId));

        int update = taskRepo.update(SQL.toString(), params);
        if (update == 0) {
            throw new TaskNotFoundException("Task not found");
        }
        return fromTaskToResponseCreateDtoMapper.toDTO(task);

    }

    public void delete(String taskId) {
        int deleted;
        try {
            deleted = taskRepo.delete(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            throw new TaskNotFoundException("Wrong task id");
        }
        if (deleted == 0) {
            throw new TaskNotFoundException("Task not found");
        }
    }
}

