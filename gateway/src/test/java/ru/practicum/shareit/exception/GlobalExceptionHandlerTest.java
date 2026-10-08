package ru.practicum.shareit.exception;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getDescription(false)).thenReturn("uri=/bookings");
    }

    @Test
    void handleMissingRequestHeader_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMissingRequestHeaderErrors(mock(MissingRequestHeaderException.class), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Отсутствует обязательный заголовок");
        assertThat(response.getBody().path()).isEqualTo("/bookings");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void handleIncorrectRequest_coversBothTypes() {
        ResponseEntity<ErrorResponse> first = handler.handleIncorrectRequestErrors(
                mock(HttpMessageNotReadableException.class), request);
        ResponseEntity<ErrorResponse> second = handler.handleIncorrectRequestErrors(
                mock(MethodArgumentTypeMismatchException.class), request);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(first.getBody().error()).isEqualTo("Ошибка парсинга запроса");
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleValidation_returnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("dto", "name", "must not be blank")));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Ошибка валидации");
    }

    @Test
    void handleConstraintViolation_stripsDottedPath() {
        ConstraintViolation<?> dotted = mock(ConstraintViolation.class);
        Path dottedPath = mock(Path.class);
        when(dottedPath.toString()).thenReturn("add.userId");
        when(dotted.getPropertyPath()).thenReturn(dottedPath);
        when(dotted.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolation<?> simple = mock(ConstraintViolation.class);
        Path simplePath = mock(Path.class);
        when(simplePath.toString()).thenReturn("userId");
        when(simple.getPropertyPath()).thenReturn(simplePath);
        when(simple.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(dotted, simple));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Нарушено ограничение");
    }

    @Test
    void handleMissingRequestParam_returnsBadRequest() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("text", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingRequestParam(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Отсутствует обязательный параметр");
    }

    @Test
    void handleHandlerMethodValidation_coversNullBranches() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        MessageSourceResolvable withCodes = mock(MessageSourceResolvable.class);
        when(withCodes.getCodes()).thenReturn(new String[]{"userId"});
        when(withCodes.getDefaultMessage()).thenReturn("must be greater than 0");
        MessageSourceResolvable nullCodes = mock(MessageSourceResolvable.class);
        when(nullCodes.getCodes()).thenReturn(null);
        when(nullCodes.getDefaultMessage()).thenReturn("bad");
        MessageSourceResolvable nullMessage = mock(MessageSourceResolvable.class);
        when(nullMessage.getCodes()).thenReturn(new String[]{"x"});
        when(nullMessage.getDefaultMessage()).thenReturn(null);
        doReturn(List.of(withCodes, nullCodes, nullMessage)).when(ex).getAllErrors();

        ResponseEntity<ErrorResponse> response = handler.handleHandlerMethodValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Ошибка валидации");
    }

    @Test
    void handleIllegalState_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalStateException(new IllegalStateException("closed"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Неверное состояние");
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Неверный параметр");
    }

    @Test
    void handleAllErrors_returnsInternalError() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAllErrors(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error()).isEqualTo("Внутренняя ошибка сервера");
        assertThat(response.getBody().path()).isEqualTo("/bookings");
    }
}
