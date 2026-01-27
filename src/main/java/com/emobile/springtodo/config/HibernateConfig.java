package com.emobile.springtodo.config;

import com.emobile.springtodo.entity.HibernateEntityTask;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
@org.springframework.context.annotation.Configuration
@Profile("hibernate")
public class HibernateConfig {


//    @Bean
//    public SessionFactory sessionFactory(){
//        Properties properties = new Properties();
//        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
//        properties.put(Environment.JAKARTA_JDBC_USER, "postgres");
//        properties.put(Environment.JAKARTA_JDBC_PASSWORD, "postgres");
//        properties.put(Environment.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
//        properties.put(Environment.JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:5432/task");
//        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
//        properties.put(Environment.HBM2DDL_AUTO, "validate");
//
//        return new Configuration()
//                .setProperties(properties)
//                .addAnnotatedClass(HibernateEntityTask.class)
//                .buildSessionFactory();
//    }

    @Autowired
    private org.springframework.core.env.Environment env;

    @Bean
    @DependsOn("flyway")
    public SessionFactory sessionFactory(Flyway flyway) {
        flyway.migrate();
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

        properties.put(Environment.JAKARTA_JDBC_URL, env.getProperty("spring.datasource.url"));
        properties.put(Environment.JAKARTA_JDBC_USER, env.getProperty("spring.datasource.username"));
        properties.put(Environment.JAKARTA_JDBC_PASSWORD, env.getProperty("spring.datasource.password"));
        properties.put(Environment.JAKARTA_JDBC_DRIVER, env.getProperty("spring.datasource.driver-class-name"));
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");

        return new org.hibernate.cfg.Configuration()
                .setProperties(properties)
                .addAnnotatedClass(HibernateEntityTask.class)
                .buildSessionFactory();
    }
}