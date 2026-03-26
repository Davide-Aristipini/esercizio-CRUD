package com.colloquio.usermanagement.dto;

public record FieldValidationErrorResponse(
        String field,
        String message
) {
}
