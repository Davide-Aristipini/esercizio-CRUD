package com.colloquio.usermanagement.controller;

import com.colloquio.usermanagement.dto.CsvImportResponse;
import com.colloquio.usermanagement.dto.CsvUploadRequest;
import com.colloquio.usermanagement.dto.ErrorResponse;
import com.colloquio.usermanagement.dto.UserRequest;
import com.colloquio.usermanagement.dto.UserResponse;
import com.colloquio.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API per la gestione degli utenti")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un nuovo utente", description = "Inserisce un nuovo utente nel database")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Utente creato con successo",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Richiesta non valida o email duplicata",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera un utente per id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Utente trovato",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse getUserById(
            @Parameter(description = "Identificativo dell'utente", example = "1")
            @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "Restituisce tutti gli utenti")
    @ApiResponse(
            responseCode = "200",
            description = "Lista utenti",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
    )
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna un utente esistente")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Utente aggiornato con successo",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Richiesta non valida o email duplicata",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse updateUser(
            @Parameter(description = "Identificativo dell'utente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un utente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utente eliminato con successo"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void deleteUser(
            @Parameter(description = "Identificativo dell'utente", example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Ricerca utenti per nome e/o cognome",
            description = "Entrambi i parametri sono opzionali. Se valorizzati insieme, vengono applicati entrambi."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Risultato della ricerca",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
    )
    public List<UserResponse> searchUsers(
            @Parameter(description = "Filtro opzionale per nome", example = "Mario")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro opzionale per cognome", example = "Rossi")
            @RequestParam(required = false) String cognome) {
        return userService.searchUsers(nome, cognome);
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Importa utenti da file CSV",
            description = "Il CSV deve contenere l'intestazione: nome,cognome,email,indirizzo",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Import completato"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "CSV non valido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CsvUploadRequest.class)
            )
    )
    public ResponseEntity<CsvImportResponse> importUsersFromCsv(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(userService.importUsersFromCsv(file));
    }
}
