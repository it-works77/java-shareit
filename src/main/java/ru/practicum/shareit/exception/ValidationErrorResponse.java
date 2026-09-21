package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ValidationErrorResponse {
    private String message;
    private Map<String, String> errors;
}
