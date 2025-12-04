package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.output.TaskResponseDTO;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FromTaskToResponseCreateDtoMapper {

    @Mapping(source = "id", target = "taskId")
    TaskResponseDTO toDTO(Task task);


}
