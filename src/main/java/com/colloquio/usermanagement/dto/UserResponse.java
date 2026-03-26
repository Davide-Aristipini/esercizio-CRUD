package com.colloquio.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rappresentazione di un utente")
public record UserResponse(
        Long id,
        String nome,
        String cognome,
        String email,
        String indirizzo
) {
}
