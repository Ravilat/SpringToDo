package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.input.CreateTaskDto;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Mapper(componentModel = "spring")
public interface FromCreateDtoToTaskMapper {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "due", ignore = true)
    @Mapping(target = "status",  ignore = true)
    @Mapping(target = "id", ignore = true)
    Task toTask(CreateTaskDto task);

    @AfterMapping
    default void setStatusAndDates(@MappingTarget Task task, CreateTaskDto createTaskDto){
        task.setStatus(Status.NEW);
        LocalDate now = LocalDate.now();
        task.setCreated(now);
        task.setDue(now.plusDays(createTaskDto.dueDate()));
    }

}
