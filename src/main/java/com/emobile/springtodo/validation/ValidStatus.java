package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatus {

    String message() default "Статус должен быть NEW, IN_PROGRESS, COMPLETED, DELETED, CANCELLED или null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
