package com.colloquio.usermanagement.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationErrorResponse> fieldErrors
) {
}
