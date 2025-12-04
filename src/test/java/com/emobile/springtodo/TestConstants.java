package com.emobile.springtodo;

import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;

import java.time.LocalDate;
import java.util.List;

public class TestConstants {

    public static Task task1 = new Task(
            "Title1",
            "descrip1",
            Status.NEW,
            1,
            LocalDate.of(2024, 12, 2),
            LocalDate.of(2024, 12, 3));

    public static Task task2 = new Task(
            "Title2",
            "descrip2",
            Status.NEW,
            2,
            LocalDate.of(2025, 12, 3),
            LocalDate.of(2025, 12, 4));

    public static Task taskCompleted = new Task(
            "Title2",
            "descrip2",
            Status.COMPLETED,
            2,
            LocalDate.of(2025, 12, 3),
            LocalDate.of(2025, 12, 4));

    public static Task taskWithId = new Task(
            1L,
            "Title1",
            "descrip1",
            Status.NEW,
            1,
            LocalDate.of(2024, 12, 2),
            LocalDate.of(2024, 12, 3));

    public static List<Task> tasksList = List.of(task1, task2);

    public static List<Task> tasksOnlyTask1 = List.of(task1);

    public static String SQL_SELECT_ALL = "SELECT * FROM tasks WHERE 1=1 ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    public static String SQL_SELECT_STATUS = "SELECT * FROM tasks WHERE 1=1 AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    public static String SQL_UPDATE_STATUS = "UPDATE tasks SET status = :status WHERE id = :id";

    public static String SQL_UPDATE_DEADLINE = "UPDATE tasks SET due_date = :due_date WHERE id = :id";

}
