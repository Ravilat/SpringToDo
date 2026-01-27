package com.emobile.springtodo.config;

import com.emobile.springtodo.mapper.FromCreateDtoToTaskMapper;
import com.emobile.springtodo.mapper.FromTaskToResponseCreateDtoMapper;
import com.emobile.springtodo.port.input.TaskUseCase;
import com.emobile.springtodo.port.output.TaskOutputManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@Configuration
public class ServiceBeans {

//    @Bean
//    public TaskUseCase manageEntityUseCase(TaskOutputManager taskOutputManager,
//                                           FromCreateDtoToTaskMapper fromCreateDtoToTaskMapper,
//                                           FromTaskToResponseCreateDtoMapper fromTaskToResponseCreateDtoMapper,
//                                           MeterRegistry meterRegistry) {
//
//        return new com.emobile.springtodo.service.TaskService(taskOutputManager,
//                fromCreateDtoToTaskMapper,
//                fromTaskToResponseCreateDtoMapper,
//                meterRegistry);
//    }

}
