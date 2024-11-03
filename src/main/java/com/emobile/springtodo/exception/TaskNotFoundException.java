package com.emobile.springtodo.exception;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
