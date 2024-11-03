package com.emobile.springtodo;

import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
public class TestConstants {

    public static Task task1;
    public static Task task2;
    public static Task task3;
    public static Task taskCompleted;
    public static Task taskWithId;
    public static Task taskForGet;

    static {
        task1 = new Task();
        task1.setTitle("Title1");
        task1.setDescription("descrip1");
        task1.setStatus(Status.NEW);
        task1.setPriority(1);
        task1.setCreated(LocalDate.of(2024, 12, 2));
        task1.setDue(LocalDate.of(2024, 12, 3));


        task2 = new Task();
        task2.setTitle("Title2");
        task2.setDescription("descrip2");
        task2.setStatus(Status.NEW);
        task2.setPriority(2);
        task2.setCreated(LocalDate.of(2025, 12, 1));
        task2.setDue(LocalDate.of(2025, 12, 6));

        taskCompleted = new Task();
        taskCompleted.setTitle("Title2");
        taskCompleted.setDescription("descrip2");
        taskCompleted.setStatus(Status.COMPLETED);
        taskCompleted.setPriority(2);
        taskCompleted.setCreated(LocalDate.of(2025, 12, 3));
        taskCompleted.setDue(LocalDate.of(2025, 12, 4));

        taskWithId = new Task();
        taskWithId.setId(1L);
        taskWithId.setTitle("Title1");
        taskWithId.setDescription("descrip1");
        taskWithId.setStatus(Status.NEW);
        taskWithId.setPriority(1);
        taskWithId.setCreated(LocalDate.of(2024, 12, 2));
        taskWithId.setDue(LocalDate.of(2024, 12, 3));

        taskForGet = new Task();
        taskForGet.setTitle("Title1");
        taskForGet.setDescription("descrip1");
        taskForGet.setStatus(Status.NEW);
        taskForGet.setPriority(1);
        taskForGet.setCreated(LocalDate.of(2024, 12, 2));
        taskForGet.setDue(LocalDate.of(2024, 12, 3));
    }


    public static List<Task> tasksList = List.of(task1, task2);

    public static List<Task> tasksOnlyTask1 = List.of(task1);

    public static final String SQL_SELECT_ALL = "SELECT * FROM tasks WHERE 1=1 ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    public static final String SQL_SELECT_STATUS = "SELECT * FROM tasks WHERE 1=1 AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    public static final String SQL_UPDATE_STATUS = "UPDATE tasks SET status = :status WHERE id = :id";

    public static final String SQL_UPDATE_DEADLINE = "UPDATE tasks SET due_date = :due_date WHERE id = :id";

}
