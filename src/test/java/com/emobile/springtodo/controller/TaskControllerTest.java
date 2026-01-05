package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.repository.TaskRepo;
import com.emobile.springtodo.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        statements = "TRUNCATE TABLE public.tasks RESTART IDENTITY CASCADE")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private TaskResponseDTO expected1;
    private TaskResponseDTO expected2;
    private List<TaskResponseDTO> expected;
    private Task task1;
    private Task task2;
    private Long id1;
    private Long id2;


    @Autowired
    TaskRepo taskRepo;

    @Autowired
    private TaskService taskService;

    private ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.0"));

    @Container
    @ServiceConnection
    public static GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:7.2.0"))
                    .withExposedPorts(6379);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @BeforeEach
    void initBase() {
        task1 = new Task(
                "Title1",
                "descrip1",
                Status.NEW,
                1,
                LocalDate.of(2024, 12, 2),
                LocalDate.of(2024, 12, 3));
        task2 = new Task(
                "Title2",
                "descrip2",
                Status.NEW,
                2,
                LocalDate.of(2025, 12, 3),
                LocalDate.of(2025, 12, 4));

        id1 = taskRepo.createTask(task1);
        id2 = taskRepo.createTask(task2);

        expected1 = TaskResponseDTO.builder()
                .taskId(id1)
                .title("Title1")
                .description("descrip1")
                .priority(1)
                .status("NEW")
                .created(LocalDate.of(2024, 12, 2))
                .due(LocalDate.of(2024, 12, 3))
                .build();

        expected2 = TaskResponseDTO.builder()
                .taskId(id2)
                .title("Title2")
                .description("descrip2")
                .priority(2)
                .status("NEW")
                .created(LocalDate.of(2025, 12, 3))
                .due(LocalDate.of(2025, 12, 4))
                .build();
        expected = Arrays.asList(expected1, expected2);
    }

    @DisplayName("Запрос задачи по id  - успешно")
    @Test
    void getTask() throws Exception {

        TaskResponseDTO expected = TaskResponseDTO.builder()
                .taskId(id1)
                .title("Title1")
                .description("descrip1")
                .priority(1)
                .status("NEW")
                .created(LocalDate.of(2024, 12, 2))
                .due(LocalDate.of(2024, 12, 3))
                .build();

        MvcResult result = mockMvc.perform(get("/todo/task/1"))
                .andExpect(status().isOk())
                .andReturn();

        String actual = result.getResponse().getContentAsString();

        String expectedJson = objectMapper.writeValueAsString(expected);

        JSONAssert.assertEquals(expectedJson, actual, false);
    }

    @Test
    @DisplayName("Запрос задачи по id - ошибка, задача не найдена")
    public void getTask_should_return_not_found() throws Exception {

        MvcResult result = mockMvc.perform(get("/todo/task/33")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResponse().getContentAsString();

        String expected = "{" +
                "\"error\":\"Task not found\"," +
                "\"message\":\"Task not found\"" +
                "}";
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Запрос задачи по id - ошибка, id не число")
    public void getTask_should_return_wrong_taskId() throws Exception {

        MvcResult result = mockMvc.perform(get("/todo/task/xx")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResponse().getContentAsString();

        String expected = "{" +
                "\"error\":\"Task not found\"," +
                "\"message\":\"Wrong task id\"" +
                "}";


        JSONAssert.assertEquals(expected, actual, false);
    }


    @Test
    @DisplayName("Запрос всех задач, без фильтрации - успешно")
    void getAllTasksWithoutFilter() throws Exception {

        MvcResult result = mockMvc.perform(get("/todo/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        String expectedJson = objectMapper.writeValueAsString(expected);

        JSONAssert.assertEquals(expectedJson, actual, false);

    }

    @Test
    @DisplayName("Запрос всех задач, с фильтрацией - успешно")
    void getAllTasksWithFilter() throws Exception {

        MvcResult resultStatus = mockMvc.perform(get("/todo/tasks?status=NEW"))
                .andExpect(status().isOk())
                .andReturn();
        String actualStatus = resultStatus.getResponse().getContentAsString();
        String expectedJsonStatus = objectMapper.writeValueAsString(expected);

        JSONAssert.assertEquals(expectedJsonStatus, actualStatus, false);

        MvcResult resultPriority = mockMvc.perform(get("/todo/tasks?priority=1"))
                .andExpect(status().isOk())
                .andReturn();
        String actualPriority = resultPriority.getResponse().getContentAsString();
        String expectedJsonPriority = objectMapper.writeValueAsString(List.of(expected1));

        JSONAssert.assertEquals(expectedJsonPriority, actualPriority, false);

        MvcResult resultCreated = mockMvc.perform(get("/todo/tasks?created=2025-01-01"))
                .andExpect(status().isOk())
                .andReturn();
        String actualCreated = resultCreated.getResponse().getContentAsString();
        String expectedJsonCreated = objectMapper.writeValueAsString(List.of(expected2));

        JSONAssert.assertEquals(expectedJsonCreated, actualCreated, false);

        MvcResult resultPage2 = mockMvc.perform(get("/todo/tasks?page=1&size=1"))
                .andExpect(status().isOk())
                .andReturn();
        String actualPage2 = resultPage2.getResponse().getContentAsString();
        String expectedJsonPage2 = objectMapper.writeValueAsString(List.of(expected1));

        JSONAssert.assertEquals(expectedJsonPage2, actualPage2, false);

    }

    @Test
    @DisplayName("Создание задачи - успешно")
    void createTask() throws Exception {
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .title("Title")
                .description("Descr")
                .priority(3)
                .dueDate(3)
                .build();

        var jsonRequest = objectMapper.writeValueAsString(createTaskDto);
        MvcResult mvcResult = mockMvc.perform(post("/todo/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();
        TaskResponseDTO actualDTO = objectMapper.readValue(actual, TaskResponseDTO.class);
        Long id = actualDTO.getTaskId();

        TaskResponseDTO expectedDto = TaskResponseDTO.builder()
                .taskId(id)
                .title("Title")
                .description("Descr")
                .status("NEW")
                .priority(3)
                .created(LocalDate.now())
                .due(LocalDate.now().plusDays(3))
                .build();
        String expected = objectMapper.writeValueAsString(expectedDto);

        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Создание задачи - ошибка валидации")
    void createTask_return_exception() throws Exception {
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .title("Title")
                .description("Descr")
                .priority(6)
                .dueDate(3)
                .build();

        var jsonRequest = objectMapper.writeValueAsString(createTaskDto);
        MvcResult mvcResult = mockMvc.perform(post("/todo/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        String expected = "{\"message\":\"validation error\"," +
                "\"errors\":" +
                "[{\"field\":\"priority\"," +
                "\"message\":\"Max value = 5\"}]}";

        JSONAssert.assertEquals(expected, actual, false);

    }

    @Test
    @DisplayName("Обновление задачи - успешно")
    void updateTask() throws Exception {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .title("NewTitle")
                .description("New descr")
                .priority(5)
                .status("COMPLETED")
                .dueDate(5)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateTaskDTO);

        MvcResult mvcResult = mockMvc.perform(patch("/todo/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        TaskResponseDTO expectedDto = TaskResponseDTO.builder()
                .taskId(1L)
                .title("NewTitle")
                .description("New descr")
                .status("COMPLETED")
                .priority(5)
                .created(LocalDate.of(2024, 12, 2))
                .due(LocalDate.of(2024, 12, 7))
                .build();
        String expected = objectMapper.writeValueAsString(expectedDto);

        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Обновление задачи - ошибка валидации")
    void updateTask_return_exception() throws Exception {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .title("NewTitleaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .description("New descr")
                .priority(5)
                .status("COMPLETED")
                .dueDate(5)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(updateTaskDTO);

        MvcResult mvcResult = mockMvc.perform(patch("/todo/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        String expected = "{\"message\":\"validation error\"," +
                "\"errors\":" +
                "[{\"field\":\"title\"," +
                "\"message\":\"title must be shorter than 20 characters\"}]}";

        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Обновление задачи - ошибка, задача не найдена")
    void updateTask_return_not_found() throws Exception {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .title("NewTitle")
                .description("New descr")
                .priority(5)
                .status("COMPLETED")
                .dueDate(5)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(updateTaskDTO);

        MvcResult mvcResult = mockMvc.perform(patch("/todo/task/33")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();
        String expected = "{" +
                "\"error\":\"Task not found\"," +
                "\"message\":\"Task not found\"" +
                "}";
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Обновление задачи - ошибка, все параметры равны null")
    void updateTask_return_null_params() throws Exception {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .title(null)
                .description(null)
                .priority(null)
                .status(null)
                .dueDate(null)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(updateTaskDTO);

        MvcResult mvcResult = mockMvc.perform(patch("/todo/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();
        String expected = "{" +
                "\"error\":\"Update parameter should not be null\"," +
                "\"message\":\"All parameters should not to be null\"" +
                "}";
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    @DisplayName("Удаление задачи - успешно")
    void deleteTask() throws Exception {
        mockMvc.perform(delete("/todo/task/1"))
                .andExpect(status().isOk());

        Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.getTask("1"));
    }

    @Test
    @DisplayName("Удаление задачи - ошибка, задача не найдена")
    void deleteTask_return_not_found() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/todo/task/33"))
                .andExpect(status().isNotFound())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();
        String expected = "{" +
                "\"error\":\"Task not found\"," +
                "\"message\":\"Task not found\"" +
                "}";
        JSONAssert.assertEquals(expected, actual, false);
    }

}
