package com.tpi.flota.config;

import com.tpi.flota.dto.ErrorResponse;
import com.tpi.flota.excepcion.DatosInvalidosException;
import com.tpi.flota.excepcion.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el servicio de flota.
 * Captura excepciones y devuelve respuestas HTTP estandarizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Recurso no encontrado",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            DatosInvalidosException ex,
            HttpServletRequest request) {
        log.warn("Datos inválidos: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Datos inválidos",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Error de validación: {}", mensaje);
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Error de validación",
            mensaje,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        String mensaje = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Violación de restricción: {}", mensaje);
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Violación de restricción",
            mensaje,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String mensaje = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // Detectar violaciones de constraint comunes y asignar códigos HTTP apropiados
        if (mensaje != null) {
            if (mensaje.contains("violates foreign key constraint")) {
                mensaje = "El recurso referenciado no existe";
                status = HttpStatus.NOT_FOUND;
            } else if (mensaje.contains("violates unique constraint") || mensaje.contains("duplicate key")) {
                mensaje = "El recurso ya existe en el sistema";
                status = HttpStatus.CONFLICT;
            } else if (mensaje.contains("violates not-null constraint")) {
                mensaje = "Faltan campos obligatorios";
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        log.warn("Violación de integridad de datos: {}", mensaje);
        ErrorResponse error = ErrorResponse.of(
            status.value(),
            "Violación de integridad de datos",
            mensaje != null ? mensaje : "Error de integridad de datos",
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Argumento inválido",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        String mensaje = ex.getMessage();
        
        if (mensaje != null) {
            if (mensaje.contains("no encontrado") || mensaje.contains("No encontrado") || 
                mensaje.contains("no existe") || mensaje.contains("No existe")) {
                log.warn("Recurso no encontrado: {}", mensaje);
                ErrorResponse error = ErrorResponse.of(
                    HttpStatus.NOT_FOUND.value(),
                    "Recurso no encontrado",
                    mensaje,
                    request.getRequestURI()
                );
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            } else if (mensaje.contains("ya existe") || mensaje.contains("Ya existe") ||
                       mensaje.contains("duplicado") || mensaje.contains("Duplicado")) {
                log.warn("Conflicto: {}", mensaje);
                ErrorResponse error = ErrorResponse.of(
                    HttpStatus.CONFLICT.value(),
                    "Conflicto",
                    mensaje,
                    request.getRequestURI()
                );
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
            } else if (mensaje.contains("inválido") || mensaje.contains("Inválido") ||
                       mensaje.contains("debe ser") || mensaje.contains("Debe ser")) {
                log.warn("Datos inválidos: {}", mensaje);
                ErrorResponse error = ErrorResponse.of(
                    HttpStatus.BAD_REQUEST.value(),
                    "Datos inválidos",
                    mensaje,
                    request.getRequestURI()
                );
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
        }
        
        log.error("RuntimeException en {}: {}", request.getRequestURI(), mensaje, ex);
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor",
            mensaje != null ? mensaje : "Error inesperado",
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
