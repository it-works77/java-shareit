package ru.practicum.shareit.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        Map<String, String> errors,
        String path
) {}
