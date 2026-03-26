package com.colloquio.usermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .description("REST API per la gestione degli utenti e l'import da CSV")
                        .version("1.0.1")
                        .contact(new Contact()
                                .name("Technical Exercise")
                                .email("davidearistipini56@gmail.com")));
    }
}
