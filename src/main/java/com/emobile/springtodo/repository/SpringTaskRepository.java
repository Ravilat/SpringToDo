package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.HibernateEntityTask;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author Ravil Sultanov
 * @since 27.01.2026
 */
@Repository
@Profile("spring")
public interface SpringTaskRepository extends JpaRepository<HibernateEntityTask, Long>, JpaSpecificationExecutor<HibernateEntityTask> {
}
