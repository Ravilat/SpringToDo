package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.port.output.TaskOutputManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 27.01.2026
 */
public class SpringTaskPersistenceAdapter implements TaskOutputManager {

    private final SpringTaskRepository springTaskRepository;

    public SpringTaskPersistenceAdapter(SpringTaskRepository springTaskRepository) {
        this.springTaskRepository = springTaskRepository;
    }

    @Override
    public Task getTask(Long id) {
        return springTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
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
            Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());

            Page<Task> resultPage = springTaskRepository
                    .findAll(TaskSpecifications.hasStatus((String) params.get("status"))
                                    .and(TaskSpecifications.hasPriority((Integer) params.get("priority")))
                                    .and(TaskSpecifications.latestCreated((Date) params.get("created")))
                                    .and(TaskSpecifications.earlyDateDue((Date) params.get("due")))
                            , pageable);
            return resultPage.get().toList();
        }
        return springTaskRepository.findAll(PageRequest.of(page, size))
                .stream()
                .toList();
    }

    @Override
    public Long createTask(Task task) {
        Task save = springTaskRepository.save(task);
        return save.getId();
    }

    @Override
    public int update(Task entityTask) {
        try {
            springTaskRepository.save(entityTask);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int delete(Long taskId) {
        if (!springTaskRepository.existsById(taskId)) {
            return 0;
        }
        try {
            springTaskRepository.deleteById(taskId);
            return 1;
        } catch (IllegalArgumentException e) {
            throw new TaskNotFoundException("Task not must be null");
        }
    }
}
