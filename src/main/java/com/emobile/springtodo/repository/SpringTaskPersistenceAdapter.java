package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.mapper.TaskEntityMapper;
import com.emobile.springtodo.port.output.TaskOutputManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 27.01.2026
 */
//@Repository
public class SpringTaskPersistenceAdapter implements TaskOutputManager {

    private final SpringTaskRepository springTaskRepository;
    private final TaskEntityMapper taskEntityMapper;

    public SpringTaskPersistenceAdapter(SpringTaskRepository springTaskRepository,
                                        TaskEntityMapper taskEntityMapper) {
        this.springTaskRepository = springTaskRepository;
        this.taskEntityMapper = taskEntityMapper;
    }

    @Override
    public Task getTask(Long id) {
        HibernateEntityTask entityTask = springTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
        return taskEntityMapper.toTaskPojo(entityTask);
    }

    @Override
    public List<Task> getAllTasks(Map<String, Object> params) {
        int size = 20;
        int offset;
        int page = 0;

        if (params != null && !params.isEmpty()) {
            size = params.get("size") == null ? 20 : (int) params.get("size");
            offset = params.get("offset") == null ? 0 : (int) params.get("offset");
            page = offset == 0 ? 0 : offset / size;
            Pageable pageable = PageRequest.of(page, size);

            Page<HibernateEntityTask> resultPage = springTaskRepository
                    .findAll(TaskSpecifications.hasStatus((String) params.get("status"))
                                    .and(TaskSpecifications.hasPriority((Integer) params.get("priority")))
                                    .and(TaskSpecifications.latestCreated((Date) params.get("created")))
                                    .and(TaskSpecifications.earlyDateDue((Date) params.get("due")))
                            , pageable);
            return resultPage.get().map(taskEntityMapper::toTaskPojo).toList();
        }
        return springTaskRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(taskEntityMapper::toTaskPojo)
                .toList();
    }

    @Override
    public Long createTask(Task task) {
        HibernateEntityTask save = springTaskRepository.save(taskEntityMapper.toEntity(task));
        return save.getId();
    }

    @Override
    public int update(Map<String, Object> params, HibernateEntityTask entityTask) {
        try {
            springTaskRepository.save(entityTask);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int delete(Long taskId) {
        springTaskRepository.deleteById(taskId);
        return springTaskRepository.existsById(taskId) ? 1 : 0;
    }
}
