package com.emobile.springtodo.dto.output;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Getter
@Setter
public class ErrorMultiplyMessageDTO {

    private String field;
    private String message;

    public ErrorMultiplyMessageDTO(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
