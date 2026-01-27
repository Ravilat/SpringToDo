package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.port.output.TaskOutputManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
//@Repository
//@Profile("jdbc")
public class JdbcTaskRepo implements TaskOutputManager {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcTaskRepo(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Task getTask(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
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

    public List<Task> getAllTasks(Map<String, Object> params) {

        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE 1=1");
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    switch (key) {
                        case "status":
                            sql.append(" AND status = :status");
                            break;
                        case "priority":
                            sql.append(" AND priority = :priority");
                            break;
                        case "created":
                            sql.append(" AND created_at >= :created");
                            break;
                        case "due":
                            sql.append(" AND due_date <= :due");
                            break;
                        case ("limit"), ("offset"):
                            break;
                    }
                }
            });

            sql.append(" ORDER BY created_at DESC");
            sql.append(" LIMIT :limit OFFSET :offset");
        }

        return namedParameterJdbcTemplate.query(sql.toString(),
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
        String sql = "INSERT INTO tasks (title, description, status, priority, created_at, due_date) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        return jdbcTemplate.queryForObject(sql,
                Long.class,
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority(),
                task.getCreated(),
                task.getDue());
    }

    public int update(Map<String, Object> params, HibernateEntityTask entityTask) {
        if (params == null) {
            throw new IllegalArgumentException("params is null");
        }
        StringBuilder sql = new StringBuilder("UPDATE tasks SET ");

        params.forEach((key, value) -> {
            if (value != null) {
                switch (key) {
                    case "title":
                        sql.append("title = :title, ");
                        break;
                    case "description":
                        sql.append("description = :description, ");
                        break;
                    case "status":
                        sql.append("status = :status, ");
                        break;
                    case "priority":
                        sql.append("priority = :priority, ");
                        break;
                    case "due_date":
                        sql.append("due_date = :due_date, ");
                        break;
                    case ("id"):
                        break;
                }
            }
        });

        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE id = :id");

        return namedParameterJdbcTemplate.update(sql.toString(), params);
    }

    public int delete(Long taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        return jdbcTemplate.update(sql, taskId);
    }
}
