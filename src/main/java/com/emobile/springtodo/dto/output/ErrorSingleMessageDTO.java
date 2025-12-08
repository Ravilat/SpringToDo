package com.emobile.springtodo.dto.output;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ravil Sultanov
 * @since 08.12.2025
 */
@Getter
@Setter
public class ErrorSingleMessageDTO {
    private String error;
    private String message;

    public ErrorSingleMessageDTO(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
