package com.sportlife.handlers;

import com.sportlife.dtos.response.ApiErrorResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejador global de excepciones.
 *
 * Patrón: @ControllerAdvice (interceptor AOP sobre todos los controllers).
 * Garantiza respuestas de error consistentes en formato JSON para toda la API.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** 404 – recurso no encontrado */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(error("NOT_FOUND", ex.getMessage()));
    }

    /** 400 – regla de negocio violada */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(error("BUSINESS_ERROR", ex.getMessage()));
    }

    /** 400 – validación de Bean Validation (@NotBlank, @Email…) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(error("VALIDATION_ERROR", msg));
    }

    /** 400 – tipo incorrecto en path variable o query param */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(error("INVALID_PARAMETER", "Parámetro inválido: " + ex.getName()));
    }

    /** 500 – cualquier error no controlado */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error("INTERNAL_ERROR", "Error interno del servidor"));
    }

    // ── Helper ────────────────────────────────────────────────────────
    private ApiErrorResponse error(String code, String message) {
        return ApiErrorResponse.builder()
            .code(code)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
