package com.emobile.springtodo.port.input;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;

import java.util.List;

public interface TaskUseCase {
    TaskResponseDTO getTask(String taskId);
    List<TaskResponseDTO> getAllTasks(TaskFilter taskFilter, int page, int size);
    TaskResponseDTO create(CreateTaskDto task);
    TaskResponseDTO update(String taskId, UpdateTaskDTO updateTaskDTO);
    void delete(String taskId);
}
