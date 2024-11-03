package com.emobile.springtodo.service;

import com.emobile.springtodo.TestConstants;
import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.dto.input.TaskFilter;
import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.TaskNotFoundException;
import com.emobile.springtodo.exception.TaskUpdateException;
import com.emobile.springtodo.mapper.*;
import com.emobile.springtodo.port.output.TaskOutputManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emobile.springtodo.TestConstants.task1;
import static com.emobile.springtodo.TestConstants.taskForGet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskOutputManager outputManager;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter completedTaskCounter;

    @Captor
    ArgumentCaptor<Task> taskCaptor;

    TaskService taskService;

    FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper = new FromTaskToResponseCreateDtoMapperImpl();

    FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper = new FromCreateDtoToTaskMapperImpl();

    FromUpdateTaskDtoToEntity fromUpdateTaskDtoToEntity = new FromUpdateTaskDtoToEntityImpl();

    @BeforeEach
    void setUp() throws Exception {
        AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("completed_tasks")).thenReturn(completedTaskCounter);
        try (autoCloseable) {
            taskService = new TaskService(outputManager,
                    fromCreateDtoToTaskMapper,
                    fromTaskToResponseCreateDtoMapper,
                    fromUpdateTaskDtoToEntity,
                    meterRegistry);
        }
    }

    @Test
    @DisplayName("Получение задачи по id - успешно")
    void getTask() {

        when(outputManager.getTask(1L)).thenReturn(taskForGet);
        var actual = taskService.getTask("1");
        verify(outputManager, times(1)).getTask(1L);
        assertEquals("Title1", actual.getTitle());
        assertEquals("NEW", actual.getStatus());
        assertEquals(LocalDate.of(2024, 12, 2), actual.getCreated());
    }

    @Test
    @DisplayName("Получение задачи по id - задача не найдена")
    void getTask_TaskNotFound() {
        when(outputManager.getTask(1L)).thenThrow(EmptyResultDataAccessException.class);
        assertThrows(TaskNotFoundException.class, () -> taskService.getTask("1"), "Task not found");
    }

    @Test
    @DisplayName("Получение задачи по id - формат id неверный")
    void getTaskFromIdNotNumber() {
        assertThrows(TaskNotFoundException.class, () -> taskService.getTask("x"), "Wrong task id");
    }

    @Test
    @DisplayName("Получение списка задач без фильтра - успешно")
    void getAllTasksWithoutFilter() {
        Map<String, Object> params = new HashMap<>();
        params.put("size", 2);
        params.put("offset", 0);
        when(outputManager.getAllTasks(params)).thenReturn(TestConstants.tasksList);

        TaskFilter taskFilter = TaskFilter.builder()
                .build();
        List<TaskResponseDTO> actualList = taskService.getAllTasks(taskFilter, 0, 2);

        assertEquals(2, actualList.size());
        assertEquals(TestConstants.tasksList.get(0).getTitle(), actualList.get(0).getTitle());
        assertEquals(TestConstants.tasksList.get(1).getTitle(), actualList.get(1).getTitle());
    }

    @Test
    @DisplayName("Получение списка задач с фильтрацией по статусу- успешно")
    void getAllTasksWithFilterWithStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "NEW");
        params.put("size", 2);
        params.put("offset", 0);
        when(outputManager.getAllTasks(params)).thenReturn(TestConstants.tasksOnlyTask1);

        TaskFilter taskFilter = TaskFilter.builder()
                .status("NEW")
                .build();
        List<TaskResponseDTO> actualList = taskService.getAllTasks(taskFilter, 0, 2);

        assertEquals(1, actualList.size());
        assertEquals(TestConstants.tasksList.getFirst().getTitle(), actualList.getFirst().getTitle());
        assertEquals(TestConstants.tasksList.getFirst().getStatus().name(), actualList.getFirst().getStatus());
    }

    @Test
    @DisplayName("Получение списка задач с пагинацией - успешно")
    void getAllTasksWithPagination() {
        Map<String, Object> params = new HashMap<>();
        params.put("size", 1);
        params.put("offset", 0);
        when(outputManager.getAllTasks(params)).thenReturn(TestConstants.tasksOnlyTask1);

        TaskFilter taskFilter = TaskFilter.builder()
                .build();
        List<TaskResponseDTO> actualList = taskService.getAllTasks(taskFilter, 0, 1);

        assertEquals(1, actualList.size());
        assertEquals(TestConstants.tasksList.getFirst().getTitle(), actualList.getFirst().getTitle());
    }

    @Test
    @DisplayName("Получение списка задач с пагинацией - успешно, результат пустой список")
    void getAllTasksWithPagination2() {
        Map<String, Object> params = new HashMap<>();
        params.put("size", 3);
        params.put("offset", 3);
        when(outputManager.getAllTasks(params)).thenReturn(List.of());

        TaskFilter taskFilter = TaskFilter.builder()
                .build();
        List<TaskResponseDTO> actualList = taskService.getAllTasks(taskFilter, 1, 3);

        assertEquals(0, actualList.size());
    }


    @Test
    @DisplayName("Создание - успешно")
    void create() {
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .title("Title1")
                .description("descrip1")
                .priority(1)
                .dueDate(5)
                .build();

        when(outputManager.createTask(argThat(t ->
                t.getTitle().equals("Title1")
                        && t.getDescription().equals("descrip1")
                        && t.getStatus().equals(Status.NEW)
                        && t.getCreated().equals(LocalDate.now()))))
                .thenReturn(1L);

        var result = taskService.create(createTaskDto);
        assertEquals(1, result.getTaskId());
        assertEquals("Title1", result.getTitle());
        assertEquals("NEW", result.getStatus());
        assertEquals(LocalDate.now(), result.getCreated());
        assertEquals(LocalDate.now().plusDays(5), result.getDue());
    }

    @Test
    @DisplayName("Создание - успешно")
    void create1() {
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .title("Title1")
                .description("descrip1")
                .priority(null)
                .dueDate(5)
                .build();

        when(outputManager.createTask(argThat(t ->
                t.getTitle().equals("Title1")
                        && t.getDescription().equals("descrip1")
                        && t.getStatus().equals(Status.NEW)
                        && t.getCreated().equals(LocalDate.now()))))
                .thenReturn(1L);

        var result = taskService.create(createTaskDto);
        assertEquals(1, result.getTaskId());
        assertEquals("Title1", result.getTitle());
        assertEquals("NEW", result.getStatus());
        assertEquals(LocalDate.now(), result.getCreated());
        assertEquals(LocalDate.now().plusDays(5), result.getDue());
    }

    @Test
    @DisplayName("Изменение - успешно")
    void update() {

        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .status("COMPLETED")
                .build();

        Task entityTask = TestConstants.taskWithId;
        fromUpdateTaskDtoToEntity.updateEntityFromDto(updateTaskDTO, entityTask);

        when(outputManager.update(entityTask)).thenReturn(1);
        when(outputManager.getTask(1L)).thenReturn(TestConstants.taskWithId);

        TaskResponseDTO update = taskService.update("1", updateTaskDTO);
        assertEquals(1, update.getTaskId());
        assertEquals("COMPLETED", update.getStatus());
        verify(outputManager, times(1)).update(entityTask);
    }

    @Test
    @DisplayName("Изменение - не найдена задача")
    void updateNotFound() {
        when(outputManager.getTask(1L)).thenThrow(EmptyResultDataAccessException.class);
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .status("COMPLETED")
                .build();
        assertThrows(TaskNotFoundException.class, () -> taskService.update("1", updateTaskDTO), "Task not found");
    }

    @Test
    @DisplayName("Изменение - некорректный формат ID")
    void updateNumberFormatException() {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .status("COMPLETED")
                .build();
        assertThrows(TaskNotFoundException.class, () -> taskService.update("x", updateTaskDTO), "Wrong task id");
    }

    @Test
    @DisplayName("Изменение - нечего изменять")
    void updateTaskUpdateException() {
        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder().build();
        when(outputManager.getTask(1L)).thenReturn(TestConstants.taskWithId);
        assertThrows(TaskUpdateException.class, () -> taskService.update("1", updateTaskDTO), "All parameters should not to be null");
    }

    @Test
    @DisplayName("Изменение - репозиторий не обновил ни одной строки")
    void updateNotFound2() {
        when(outputManager.getTask(1L)).thenReturn(task1);

        UpdateTaskDTO updateTaskDTO = UpdateTaskDTO.builder()
                .status("COMPLETED")
                .build();

        Task entityTask = task1;
        fromUpdateTaskDtoToEntity.updateEntityFromDto(updateTaskDTO, entityTask);

        when(outputManager.update(entityTask)).thenReturn(0);

        assertThrows(TaskNotFoundException.class, () -> taskService.update("1", updateTaskDTO), "Task not found");
    }

    @Test
    @DisplayName("Удаление - некорректный формат ID")
    void deleteNumberFormatException() {
        assertThrows(TaskNotFoundException.class, () -> taskService.delete("x"));
    }

    @Test
    @DisplayName("Удаление - нет записи")
    void deleteNotFound() {
        when(outputManager.delete(1L)).thenReturn(0);
        assertThrows(TaskNotFoundException.class, () -> taskService.delete("1"));
    }
}