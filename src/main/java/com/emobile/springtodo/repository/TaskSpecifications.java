package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

/**
 * @author Ravil Sultanov
 * @since 27.01.2026
 */
public class TaskSpecifications {

    public static Specification<Task> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Task> hasPriority(Integer priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null || priority == 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Task> latestCreated(Date created) {
        return (root, query, criteriaBuilder) -> {
            if (created == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("created"), created);
        };
    }

    public static Specification<Task> earlyDateDue(Date due) {
        return (root, query, criteriaBuilder) -> {
            if (due == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("created"), due);
        };
    }
}
