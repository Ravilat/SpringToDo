package com.emobile.springtodo.exception;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
public class TaskUpdateException extends RuntimeException {
    public TaskUpdateException(String message) {
        super(message);
    }
}
