package com.emobile.springtodo.config;

import com.emobile.springtodo.port.output.TaskOutputManager;
import com.emobile.springtodo.repository.SpringTaskPersistenceAdapter;
import com.emobile.springtodo.repository.SpringTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@Configuration
public class PersistenceBeans {

    @Bean
    public TaskOutputManager springPersistenceAdapter(SpringTaskRepository springTaskRepository) {
        return new SpringTaskPersistenceAdapter(springTaskRepository);
    }
}
