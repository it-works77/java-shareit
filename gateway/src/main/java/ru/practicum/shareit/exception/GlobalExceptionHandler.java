package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderErrors(MissingRequestHeaderException ex,
                                                                          WebRequest request) {
        log.warn("Отсутствует обязательный заголовок: {}", ex.getMessage());
        log.debug("Отсутствует обязательный заголовок", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Отсутствует обязательный заголовок", request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleIncorrectRequestErrors(Exception ex, WebRequest request) {
        log.warn("Ошибка парсинга запроса: {}", ex.getMessage());
        log.debug("Ошибка парсинга запроса", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Ошибка парсинга запроса", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                                    WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Ошибка валидации: {}", errors);
        log.debug("Ошибка валидации", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                             WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errors.put(field, violation.getMessage());
        });
        log.warn("Нарушено ограничение: {}", errors);
        log.debug("Нарушено ограничение", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Нарушено ограничение", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException ex,
                                                                   WebRequest request) {
        log.warn("Отсутствует обязательный параметр: {}", ex.getMessage());
        log.debug("Отсутствует обязательный параметр", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Отсутствует обязательный параметр", request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                       WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getAllErrors().forEach(error -> {
            String[] codes = error.getCodes();
            String field = (codes != null && codes.length > 0) ? codes[0] : "unknown";
            String message = error.getDefaultMessage();
            if (message != null) {
                errors.put(field, message);
            }
        });
        log.warn("Ошибка валидации параметров: {}", errors);
        log.debug("Ошибка валидации параметров", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        log.warn("Неверное состояние: {}", ex.getMessage());
        log.debug("Неверное состояние", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Неверное состояние", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Неверный параметр: {}", ex.getMessage());
        log.debug("Неверный параметр", ex);

        return createResponse(HttpStatus.BAD_REQUEST, "Неверный параметр", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllErrors(Exception ex, WebRequest request) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage());
        log.debug("Внутренняя ошибка сервера:", ex);

        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request);
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        status.value(),
                        message,
                        request.getDescription(false).replace("uri=", "")
                ));
    }
}
