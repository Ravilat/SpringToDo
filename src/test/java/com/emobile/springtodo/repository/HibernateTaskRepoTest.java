package com.emobile.springtodo.repository;

import com.emobile.springtodo.TestConstants;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.mapper.FromUpdateTaskDtoToEntity;
import com.emobile.springtodo.mapper.TaskEntityMapper;
import com.emobile.springtodo.port.output.TaskOutputManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emobile.springtodo.TestConstants.*;
import static com.emobile.springtodo.TestConstants.task1;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("hibernate")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        statements = "TRUNCATE TABLE public.tasks RESTART IDENTITY CASCADE")
public class HibernateTaskRepoTest {
    @Autowired
    TaskOutputManager outputManager;

    @Autowired
    TaskEntityMapper taskEntityMapper;

    @Autowired
    FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @DisplayName("Сохранение и получение задачи - успешно")
    void getAndCreateTest() {
        Long id = outputManager.createTask(task1);
        assertDoesNotThrow(() -> outputManager.createTask(task1));
        Task task1 = outputManager.getTask(id);
        assertDoesNotThrow(() -> outputManager.getTask(id));
        assertEquals(TestConstants.task1.getTitle(), task1.getTitle());
        assertEquals(TestConstants.task1.getDescription(), task1.getDescription());
        assertEquals(TestConstants.task1.getStatus(), task1.getStatus());
        assertEquals(TestConstants.task1.getPriority(), task1.getPriority());
        assertEquals(TestConstants.task1.getCreated(), task1.getCreated());
        assertEquals(TestConstants.task1.getDue(), task1.getDue());
    }

    @Test
    @DisplayName("Получение задачи - ошибка, нет задачи")
    void getTask_return_null() {
        Task task = outputManager.getTask(33L);
        assertNull(task);
    }

    @Test
    @DisplayName("Получение задач - успешно")
    void getAllTasks() {
        outputManager.createTask(task1);
        outputManager.createTask(taskCompleted);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("limit", 2);
        params1.put("offset", 0);
        List<Task> result1 = outputManager.getAllTasks(params1);
        assertDoesNotThrow(() -> outputManager.getAllTasks(params1));
        assertEquals(2, result1.size());
        assertEquals(task2.getTitle(), result1.get(0).getTitle());
        assertEquals(task1.getTitle(), result1.get(1).getTitle());
    }

    @Test
    @DisplayName("Получение задач по статусу - успешно")
    void getAllTasksByStatus() {
        outputManager.createTask(task2);
        outputManager.createTask(taskCompleted);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("status", "NEW");
        params2.put("limit", 2);
        params2.put("offset", 0);
        List<Task> result2 = outputManager.getAllTasks(params2);
        assertDoesNotThrow(() -> outputManager.getAllTasks(params2));
        assertEquals(1, result2.size());
        assertEquals(task2.getTitle(), result2.get(0).getTitle());
    }


    @Test
    @DisplayName("Изменение задачи - успешно")
    void update() {
        Long id1 = outputManager.createTask(task1);
        Long id2 = outputManager.createTask(task2);
        task1.setId(id1);
        task2.setId(id2);

        Date updateDueDate = Date.valueOf(LocalDate.of(2025, 12, 9));

        Map<String, Object> params1 = new HashMap<>();
        params1.put("status", "COMPLETED");
        params1.put("id", id1);

        outputManager.update(params1, null);
        Task actual1 = outputManager.getTask(id1);
        Task actual2 = outputManager.getTask(id2);
        assertEquals(Status.COMPLETED, actual1.getStatus());
        assertEquals(Status.NEW, actual2.getStatus());

        Map<String, Object> params2 = new HashMap<>();
        params2.put("due_date", updateDueDate);
        params2.put("id", id2);
        outputManager.update(params2, null);

        actual2 = outputManager.getTask(id2);
        assertEquals(updateDueDate, Date.valueOf(actual2.getDue()));
    }

    @Test
    @DisplayName("Изменение задачи - задача не найдена")
    void update_return_error() {
        Map<String, Object> params1 = new HashMap<>();
        params1.put("status", "COMPLETED");
        params1.put("id", 1);
        int update = outputManager.update(params1, null);
        assertEquals(0, update);
    }


    @Test
    @DisplayName("Удаление задачи - успешно")
    void delete() {
        Long id = outputManager.createTask(task1);
        int delete = outputManager.delete(id);
        assertEquals(1, delete);
        Task task = outputManager.getTask(id);
        assertNull(task);
        List<Task> tasks = outputManager.getAllTasks(null);
        assertEquals(0, tasks.size());

        int delete0 = outputManager.delete(id);
        assertEquals(0, delete0);
    }
}
