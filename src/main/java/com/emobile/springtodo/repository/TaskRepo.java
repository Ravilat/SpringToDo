package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TaskRepo {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TaskRepo(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Task getTask(Long id) {
        String SQL = "SELECT * FROM tasks WHERE id = ?";

        return jdbcTemplate.queryForObject(SQL, (rs, rowNum) ->
                new Task(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        Status.valueOf(rs.getString("status")),
                        rs.getInt("priority"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("due_date").toLocalDate()
                ), id);
    }

    public List<Task> getAllTasks(String SQL, Map<String, Object> params) {

        return namedParameterJdbcTemplate.query(SQL,
                params,
                (rs, rowNum) -> new Task(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        Status.valueOf(rs.getString("status")),
                        rs.getInt("priority"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("due_date").toLocalDate()
                ));
    }

    public Long createTask(Task task) {
        String SQL = "INSERT INTO tasks (title, description, status, priority, created_at, due_date) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        return jdbcTemplate.queryForObject(SQL,
                Long.class,
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority(),
                task.getCreated(),
                task.getDue());
    }

    public int update(String SQL, Map<String, Object> params) {
        return namedParameterJdbcTemplate.update(SQL, params);
    }

    public int delete(Long taskId) {
        String SQL = "DELETE FROM tasks WHERE id = ?";
        return jdbcTemplate.update(SQL, taskId);
    }
}
