package com.colloquio.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.colloquio.usermanagement.dto.UserResponse;
import com.colloquio.usermanagement.entity.User;
import com.colloquio.usermanagement.mapper.UserMapper;
import com.colloquio.usermanagement.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=false"
})
@Import({UserService.class, UserMapper.class, UserSearchIntegrationTest.TestConfig.class})
class UserSearchIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(createUser("Mario", "Rossi", "mario.rossi@example.com", "Via Roma 1"));
        userRepository.save(createUser("Maria", "Bianchi", "maria.bianchi@example.com", "Via Milano 2"));
        userRepository.save(createUser("Luigi", "Rossi", "luigi.rossi@example.com", "Corso Italia 3"));
    }

    @Test
    void searchUsersShouldFilterByNomeAndCognome() {
        List<UserResponse> byNome = userService.searchUsers("mari", null);
        List<UserResponse> byCognome = userService.searchUsers(null, "rossi");
        List<UserResponse> byNomeAndCognome = userService.searchUsers("mario", "rossi");

        assertThat(byNome)
                .extracting(UserResponse::email)
                .containsExactlyInAnyOrder("mario.rossi@example.com", "maria.bianchi@example.com");

        assertThat(byCognome)
                .extracting(UserResponse::email)
                .containsExactlyInAnyOrder("mario.rossi@example.com", "luigi.rossi@example.com");

        assertThat(byNomeAndCognome)
                .extracting(UserResponse::email)
                .containsExactly("mario.rossi@example.com");
    }

    private User createUser(String nome, String cognome, String email, String indirizzo) {
        User user = new User();
        user.setNome(nome);
        user.setCognome(cognome);
        user.setEmail(email);
        user.setIndirizzo(indirizzo);
        return user;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }
    }
}
