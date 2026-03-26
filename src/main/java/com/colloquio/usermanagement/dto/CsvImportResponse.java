package com.colloquio.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Riepilogo del risultato di un import CSV")
public record CsvImportResponse(
        int importedCount,
        int discardedCount,
        List<CsvImportError> discardedRows
) {
}
