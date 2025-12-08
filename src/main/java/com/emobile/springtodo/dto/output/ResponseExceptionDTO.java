package com.emobile.springtodo.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Getter
@Setter
public class ResponseExceptionDTO {

    private String message;
    private List<ErrorMultiplyMessageDTO> errors;

    public ResponseExceptionDTO(String message, List<ErrorMultiplyMessageDTO> errors) {
        this.message = message;
        this.errors = errors;
    }
}
