package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.input.UpdateTaskDTO;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * @author Ravil Sultanov
 * @since 27.01.2026
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FromUpdateTaskDtoToEntity {

    void updateEntityFromDto(UpdateTaskDTO updateTaskDTO, @MappingTarget Task entity);

    @AfterMapping
    default void setDueDate(UpdateTaskDTO updateTaskDTO, @MappingTarget Task entity) {
        if (updateTaskDTO != null && updateTaskDTO.dueDate() != null) {
            if (entity.getCreated() != null) {
                entity.setDue(entity.getCreated().plusDays(updateTaskDTO.dueDate()));
            }
        }
    }
}
