package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderErrors(EntityNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message("Отсутствует обязательный заголовок")
                .details(ex.getMessage())
                .build();
        log.warn("Отсутствует обязательный заголовок: {}", ex.getMessage());
        log.debug("Отсутствует обязательный заголовок", ex);
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
