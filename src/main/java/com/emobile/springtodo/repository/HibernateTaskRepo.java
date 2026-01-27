package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.HibernateEntityTask;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.mapper.TaskEntityMapper;
import com.emobile.springtodo.port.output.TaskOutputManager;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;

/**
 * @author Ravil Sultanov
 * @since 26.01.2026
 */
//@Component
//@Profile("hibernate")
public class HibernateTaskRepo implements TaskOutputManager {

    private final SessionFactory sessionFactory;
    private final TaskEntityMapper taskEntityMapper;

    public HibernateTaskRepo(SessionFactory sessionFactory, TaskEntityMapper taskEntityMapper) {
        this.sessionFactory = sessionFactory;
        this.taskEntityMapper = taskEntityMapper;
    }

    @Override
    public Task getTask(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            HibernateEntityTask entityTask = sessionFactory.getCurrentSession().find(HibernateEntityTask.class, id);
            Task task = taskEntityMapper.toTaskPojo(entityTask);
            transaction.commit();
            return task;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка получения задачи: " + ex.getMessage());
        }
    }

    @Override
    public List<Task> getAllTasks(Map<String, Object> params) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<HibernateEntityTask> cq = cb.createQuery(HibernateEntityTask.class);
            Root<HibernateEntityTask> root = cq.from(HibernateEntityTask.class);
            int size = 20;
            int offset = 0;
            List<Predicate> predicates = new ArrayList<>();
            if (params != null) {
                params.forEach((key, value) -> {
                    if (value != null) {
                        switch (key) {
                            case "status", "priority":
                                predicates.add(cb.equal(root.get(key), value));
                                break;
                            case "created":
                                predicates.add(cb.greaterThanOrEqualTo(root.get(key), (Date) value));
                                break;
                            case "due":
                                predicates.add(cb.lessThanOrEqualTo(root.get(key), (Date) value));
                                break;
                            case ("limit"), ("offset"):
                                break;
                        }
                    }
                });
                size = params.get("size") == null ? 20 : (int) params.get("size");
                offset = params.get("offset") == null ? 0 : (int) params.get("offset");
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            cq.orderBy(cb.desc(root.get("created")));

            List<HibernateEntityTask> entityTaskList = sessionFactory.getCurrentSession().createQuery(cq)
                    .setFirstResult(offset)
                    .setMaxResults(size)
                    .getResultList();
            List<Task> list = entityTaskList.stream().map(taskEntityMapper::toTaskPojo).toList();
            transaction.commit();
            return list;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка поиска задач: " + ex.getMessage());
        }
    }

    @Override
    public Long createTask(Task task) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            HibernateEntityTask entity = taskEntityMapper.toEntity(task);
            session.persist(entity);
            session.flush();
            transaction.commit();
            return entity.getId();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка создания задачи: " + ex.getMessage());
        }
    }

    @Override
    public int update(Map<String, Object> params, HibernateEntityTask entityTask) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

//            session.merge(entityTask);
//
//            session.flush();

            CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaUpdate<HibernateEntityTask> update = cb.createCriteriaUpdate(HibernateEntityTask.class);
            Root<HibernateEntityTask> root = update.from(HibernateEntityTask.class);

            params.forEach((key, value) -> {
                if (value != null) {
                    switch (key) {
                        case "title", "description", "priority", "status":
                            update.set(root.get(key), value);
                            break;
                        case "due_date":
                            update.set(root.get("due"), value);
                            break;
                        case ("id"):
                            break;
                    }
                }
            });
            update.where(cb.equal(root.get("id"), params.get("id")));
            int updateCount = session.createMutationQuery(update).executeUpdate();

            transaction.commit();
            return updateCount;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
                return 0;
            }
            throw new RuntimeException("Ошибка изменения задачи: " + ex.getMessage());
        }
    }

    @Override
    public int delete(Long taskId) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            HibernateEntityTask entityTask = session.find(HibernateEntityTask.class, taskId);
            if (entityTask != null) {
                session.remove(entityTask);
                transaction.commit();
                return 1;
            }
            return 0;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                return 0;
            }
            throw new RuntimeException("Ошибка удаления задачи: " + e.getMessage());
        }
    }
}
