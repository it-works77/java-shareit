package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAllErrors_returnsServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleAllErrors(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleAccessDenied_returnsForbidden() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDeniedException(new AccessDeniedException("no"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleEntityNotFound_returnsNotFound() {
        ResponseEntity<ErrorResponse> response =
                handler.handleEntityNotFoundErrors(new EntityNotFoundException("no"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleAlreadyExists_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler
                .handleEntityAlreadyExistsErrors(new EntityAlreadyExistsException("dup"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleItemNotAvailable_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleItemIsNotAvailable(new ItemIsNotAvailableException("busy"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleIllegalState_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalStateException(new IllegalStateException("bad"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
