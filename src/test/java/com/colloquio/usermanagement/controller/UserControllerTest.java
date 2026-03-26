package com.colloquio.usermanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.colloquio.usermanagement.dto.UserResponse;
import com.colloquio.usermanagement.exception.GlobalExceptionHandler;
import com.colloquio.usermanagement.exception.ResourceNotFoundException;
import com.colloquio.usermanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUserShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                  "nome": "",
                  "cognome": "Rossi",
                  "email": "email-non-valida",
                  "indirizzo": ""
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Richiesta non valida"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='nome')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='email')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='indirizzo')]").exists());
    }

    @Test
    void getUserByIdShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("Utente con id 99 non trovato"));

        mockMvc.perform(get("/api/users/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utente con id 99 non trovato"));
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        UserResponse response = new UserResponse(1L, "Mario", "Rossi", "mario.rossi@example.com", "Via Roma 1");
        String payload = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario.rossi@example.com",
                  "indirizzo": "Via Roma 1"
                }
                """;

        when(userService.createUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("mario.rossi@example.com"));
    }
}
