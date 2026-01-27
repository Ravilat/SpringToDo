package com.emobile.springtodo.port.output;

import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
public interface TaskOutputManager {
    Task getTask(Long id);

    List<Task> getAllTasks(Map<String, Object> params);

    Long createTask(Task task);

    int update(Map<String, Object> params, HibernateEntityTask  task);

    int delete(Long taskId);
}
