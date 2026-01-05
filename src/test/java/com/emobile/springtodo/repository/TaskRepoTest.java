package com.emobile.springtodo.repository;

import com.emobile.springtodo.TestConstants;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@DataJpaTest
@Import(TaskRepo.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepoTest {

    @Autowired
    TaskRepo taskRepo;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.0"));

    @Test
    @DisplayName("Сохранение и получение задачи - успешно")
    void getAndCreateTest() {
        Long id = taskRepo.createTask(TestConstants.task1);
        assertDoesNotThrow(() -> taskRepo.createTask(TestConstants.task1));
        Task task1 = taskRepo.getTask(id);
        assertDoesNotThrow(() -> taskRepo.getTask(id));
        assertEquals(TestConstants.task1.getTitle(), task1.getTitle());
        assertEquals(TestConstants.task1.getDescription(), task1.getDescription());
        assertEquals(TestConstants.task1.getStatus(), task1.getStatus());
        assertEquals(TestConstants.task1.getPriority(), task1.getPriority());
        assertEquals(TestConstants.task1.getCreated(), task1.getCreated());
        assertEquals(TestConstants.task1.getDue(), task1.getDue());
    }

    @Test
    @DisplayName("Получение задачи - ошибка, нет задачи")
    void getTask_return_error() {
        assertThrows(EmptyResultDataAccessException.class, () -> taskRepo.getTask(33L));
    }

    @Test
    @DisplayName("Получение задач - успешно")
    void getAllTasks() {
        taskRepo.createTask(TestConstants.task1);
        taskRepo.createTask(TestConstants.taskCompleted);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("limit", 2);
        params1.put("offset", 0);
        List<Task> result1 = taskRepo.getAllTasks(TestConstants.SQL_SELECT_ALL, params1);
        assertDoesNotThrow(() -> taskRepo.getAllTasks(TestConstants.SQL_SELECT_ALL, params1));
        assertEquals(2, result1.size());
        assertEquals(TestConstants.task2.getTitle(), result1.get(0).getTitle());
        assertEquals(TestConstants.task1.getTitle(), result1.get(1).getTitle());
    }

    @Test
    @DisplayName("Получение задач по статусу - успешно")
    void getAllTasksByStatus() {
        taskRepo.createTask(TestConstants.task2);
        taskRepo.createTask(TestConstants.taskCompleted);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("status", "NEW");
        params2.put("limit", 2);
        params2.put("offset", 0);
        List<Task> result2 = taskRepo.getAllTasks(TestConstants.SQL_SELECT_STATUS, params2);
        assertDoesNotThrow(() -> taskRepo.getAllTasks(TestConstants.SQL_SELECT_STATUS, params2));
        assertEquals(1, result2.size());
        assertEquals(TestConstants.task2.getTitle(), result2.get(0).getTitle());
    }


    @Test
    @DisplayName("Изменение задачи - успешно")
    void update() {
        Long id1 = taskRepo.createTask(TestConstants.task1);
        Long id2 = taskRepo.createTask(TestConstants.task2);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("status", "COMPLETED");
        params1.put("id", id1);
        taskRepo.update(TestConstants.SQL_UPDATE_STATUS, params1);
        Task actual1 = taskRepo.getTask(id1);
        Task actual2 = taskRepo.getTask(id2);
        assertEquals(Status.COMPLETED, actual1.getStatus());
        assertEquals(Status.NEW, actual2.getStatus());

        Map<String, Object> params2 = new HashMap<>();
        params2.put("due_date", Date.valueOf(LocalDate.now()));
        params2.put("id", id1);
        taskRepo.update(TestConstants.SQL_UPDATE_DEADLINE, params2);
        Task actual3 = taskRepo.getTask(id1);
        assertEquals(LocalDate.now(), actual3.getDue());
    }

    @Test
    @DisplayName("Изменение задачи - задача не найдена")
    void update_return_error() {
        Map<String, Object> params1 = new HashMap<>();
        params1.put("status", "COMPLETED");
        params1.put("id", 1);
        int update = taskRepo.update(TestConstants.SQL_UPDATE_STATUS, params1);
        assertEquals(0, update);
    }


    @Test
    @DisplayName("Удаление задачи - успешно")
    void delete() {
        Long id = taskRepo.createTask(TestConstants.task1);
        int delete = taskRepo.delete(id);
        assertEquals(1, delete);
        assertThrows(EmptyResultDataAccessException.class, () -> taskRepo.getTask(id));
        List<Task> tasks = taskRepo.getAllTasks("SELECT * FROM tasks WHERE 1=1", null);
        assertEquals(0, tasks.size());

        int delete0 = taskRepo.delete(id);
        assertEquals(0, delete0);
    }
}