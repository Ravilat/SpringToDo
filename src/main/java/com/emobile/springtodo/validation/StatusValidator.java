package com.emobile.springtodo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
public class StatusValidator implements ConstraintValidator<ValidStatus, String> {

    private final Set<String> roles = Set.of("NEW", "IN_PROGRESS", "COMPLETED", "DELETED", "CANCELLED");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || roles.contains(value);
    }
}
