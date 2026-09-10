package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private String details;

    public static class EntityAlreadyExistsException extends RuntimeException {
        public EntityAlreadyExistsException(String message) {
            super(message);
        }
    }
}
