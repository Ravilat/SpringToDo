package com.emobile.springtodo.config;

import com.emobile.springtodo.mapper.TaskEntityMapper;
import com.emobile.springtodo.port.output.TaskOutputManager;
import com.emobile.springtodo.repository.HibernateTaskRepo;
import com.emobile.springtodo.repository.JdbcTaskRepo;
import com.emobile.springtodo.repository.SpringTaskPersistenceAdapter;
import com.emobile.springtodo.repository.SpringTaskRepository;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@Configuration
public class PersistenceBeans {

    @Bean
    @Profile("jdbc")
    public TaskOutputManager taskJdbcOutputManager(JdbcTemplate jdbcTemplate,
                                                   NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new JdbcTaskRepo(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Bean
    @Profile("hibernate")
    public TaskOutputManager taskHiberOutputManager(SessionFactory sessionFactory,
                                                    TaskEntityMapper taskEntityMapper) {
        return new HibernateTaskRepo(sessionFactory, taskEntityMapper);
    }

    @Bean
    @Profile("spring")
    public TaskOutputManager springPersistenceAdapter(SpringTaskRepository springTaskRepository,
                                                      TaskEntityMapper taskEntityMapper) {
        return new SpringTaskPersistenceAdapter(springTaskRepository, taskEntityMapper);
    }
}
