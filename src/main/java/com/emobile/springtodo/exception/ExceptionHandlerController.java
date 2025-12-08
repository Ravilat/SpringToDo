package com.emobile.springtodo.exception;

import com.emobile.springtodo.dto.output.ErrorMultiplyMessageDTO;
import com.emobile.springtodo.dto.output.ErrorSingleMessageDTO;
import com.emobile.springtodo.dto.output.ResponseExceptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@RestControllerAdvice
public class ExceptionHandlerController {


    @ExceptionHandler({TaskNotFoundException.class})
    public ResponseEntity<?> onEntityNotFoundException(RuntimeException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorSingleMessageDTO("Task not found", e.getMessage()));
    }

    @ExceptionHandler({TaskUpdateException.class})
    public ResponseEntity<?> updateParameterException(RuntimeException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorSingleMessageDTO("Update parameter should not be null", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<ErrorMultiplyMessageDTO> errorList = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(
                err -> errorList.add(new ErrorMultiplyMessageDTO(err.getField(), err.getDefaultMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseExceptionDTO("validation error", errorList));
    }

}
