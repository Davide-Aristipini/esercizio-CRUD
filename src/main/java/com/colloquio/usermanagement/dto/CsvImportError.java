package com.colloquio.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dettaglio di una riga CSV scartata")
public record CsvImportError(
        long rowNumber,
        String message
) {
}
