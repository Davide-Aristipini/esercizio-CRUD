package com.colloquio.usermanagement.config;

import com.colloquio.usermanagement.dto.CsvImportResponse;
import com.colloquio.usermanagement.repository.UserRepository;
import com.colloquio.usermanagement.service.UserService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@EnableConfigurationProperties(MockDataProperties.class)
public class DatabaseStartupInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStartupInitializer.class);
    private static final String MOCKUP_CSV_PATH = "mockup/users-mockup.csv";

    @Bean
    public ApplicationRunner databaseStartupRunner(
            DataSource dataSource,
            UserRepository userRepository,
            UserService userService,
            MockDataProperties mockDataProperties) {
        return args -> {
            verifyUsersTable(dataSource);
            seedMockUsersIfNeeded(userRepository, userService, mockDataProperties);
        };
    }

    private void verifyUsersTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            if (tableExists(metaData, "users") || tableExists(metaData, "USERS")) {
                LOGGER.info("Verifica database completata: tabella 'users' presente.");
                return;
            }

            throw new IllegalStateException("Verifica database fallita: tabella 'users' non trovata.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossibile verificare la presenza della tabella 'users'.", exception);
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private void seedMockUsersIfNeeded(
            UserRepository userRepository,
            UserService userService,
            MockDataProperties mockDataProperties) {
        if (!mockDataProperties.isEnabled()) {
            LOGGER.info("Caricamento dati mock disabilitato da configurazione.");
            return;
        }

        if (userRepository.count() > 0) {
            LOGGER.info("Database gia' popolato: nessun dato mock inserito.");
            return;
        }

        Resource mockupCsv = new ClassPathResource(MOCKUP_CSV_PATH);
        CsvImportResponse response = userService.importUsersFromCsvResource(mockupCsv);
        LOGGER.info(
                "Import mock iniziale completato da '{}': importati={}, scartati={}.",
                MOCKUP_CSV_PATH,
                response.importedCount(),
                response.discardedCount()
        );
    }
}
