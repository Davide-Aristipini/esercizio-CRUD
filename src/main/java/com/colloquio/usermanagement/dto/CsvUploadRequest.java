package com.colloquio.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsvUploadRequest", description = "Payload multipart per l'import utenti da CSV")
public class CsvUploadRequest {

    @Schema(
            description = "File CSV con intestazione nome,cognome,email,indirizzo",
            type = "string",
            format = "binary"
    )
    private Object file;

    public Object getFile() {
        return file;
    }

    public void setFile(Object file) {
        this.file = file;
    }
}
