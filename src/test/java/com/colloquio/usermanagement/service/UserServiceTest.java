package com.colloquio.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.colloquio.usermanagement.dto.CsvImportResponse;
import com.colloquio.usermanagement.dto.UserRequest;
import com.colloquio.usermanagement.entity.User;
import com.colloquio.usermanagement.exception.DuplicateEmailException;
import com.colloquio.usermanagement.mapper.UserMapper;
import com.colloquio.usermanagement.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        userService = new UserService(userRepository, new UserMapper(), validator);
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        UserRequest request = new UserRequest("Mario", "Rossi", "Mario.Rossi@example.com", "Via Roma 1");
        when(userRepository.existsByEmail("mario.rossi@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("mario.rossi@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void importUsersFromCsvShouldImportValidRowsAndDiscardInvalidOnes() {
        String csv = """
                nome,cognome,email,indirizzo
                Mario,Rossi,mario.rossi@example.com,Via Roma 1
                ,Bianchi,mail-non-valida,Via Milano 2
                Luca,Verdi,mario.rossi@example.com,Piazza Duomo 8
                Giulia,Neri,giulia.neri@example.com,Corso Italia 15
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                csv.getBytes()
        );

        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CsvImportResponse response = userService.importUsersFromCsv(file);

        assertThat(response.importedCount()).isEqualTo(2);
        assertThat(response.discardedCount()).isEqualTo(2);
        assertThat(response.discardedRows())
                .hasSize(2)
                .anySatisfy(error -> {
                    assertThat(error.rowNumber()).isEqualTo(3);
                    assertThat(error.message()).contains("L'email non e' valida");
                    assertThat(error.message()).contains("Il nome e' obbligatorio");
                })
                .anySatisfy(error -> {
                    assertThat(error.rowNumber()).isEqualTo(4);
                    assertThat(error.message()).contains("Email duplicata");
                });
    }
}
