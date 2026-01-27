package com.emobile.springtodo.mapper;

import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.Mapper;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@Mapper(componentModel = "spring")
public interface TaskEntityMapper {
    Task toTaskPojo(HibernateEntityTask task);

    HibernateEntityTask toEntity(Task domain);
}
