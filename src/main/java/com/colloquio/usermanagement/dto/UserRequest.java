package com.colloquio.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload per creare o aggiornare un utente")
public record UserRequest(
        @Schema(example = "Mario")
        @NotBlank(message = "Il nome e' obbligatorio")
        @Size(max = 100, message = "Il nome non puo' superare 100 caratteri")
        String nome,

        @Schema(example = "Rossi")
        @NotBlank(message = "Il cognome e' obbligatorio")
        @Size(max = 100, message = "Il cognome non puo' superare 100 caratteri")
        String cognome,

        @Schema(example = "mario.rossi@example.com")
        @NotBlank(message = "L'email e' obbligatoria")
        @Email(message = "L'email non e' valida")
        @Size(max = 255, message = "L'email non puo' superare 255 caratteri")
        String email,

        @Schema(example = "Via Roma 1, Milano")
        @NotBlank(message = "L'indirizzo e' obbligatorio")
        @Size(max = 255, message = "L'indirizzo non puo' superare 255 caratteri")
        String indirizzo
) {
}
