package com.tpi.logistica.dto;

import java.time.LocalDateTime;

/**
 * DTO estándar para respuestas de error HTTP.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
}
