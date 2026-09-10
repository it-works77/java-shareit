package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundErrors(EntityNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Объект не найден")
                .details(ex.getMessage())
                .build();
        log.warn("Объект не найден: {}", ex.getMessage());
        log.debug("Объект не найден", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderErrors(MissingRequestHeaderException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Отсутствует обязательный заголовок")
                .details(ex.getMessage())
                .build();
        log.warn("Отсутствует обязательный заголовок: {}", ex.getMessage());
        log.debug("Отсутствует обязательный заголовок", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .message("Ошибка валидации")
                .errors(errors)
                .build();
        log.warn("Ошибка валидации: {}", errors);
        log.debug("Ошибка валидации", ex);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchErrors(MethodArgumentTypeMismatchException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Неверный тип аргумента")
                .details(ex.getMessage())
                .build();
        log.warn("Неверный тип аргумента: {}", ex.getMessage());
        log.debug("Неверный тип аргумента", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableErrors(HttpMessageNotReadableException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Ошибка парсинга запроса")
                .details(ex.getMessage())
                .build();
        log.warn("Ошибка парсинга запроса: {}", ex.getMessage());
        log.debug("Ошибка парсинга запроса", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllErrors(Exception ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Внутренняя ошибка сервера")
                .details(ex.getMessage())
                .build();
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage());
        log.debug("Внутренняя ошибка сервера:", ex);
        return ResponseEntity.internalServerError().body(body);
    }
}
