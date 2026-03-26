package com.colloquio.usermanagement.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.colloquio.usermanagement.dto.CsvImportResponse;
import com.colloquio.usermanagement.repository.UserRepository;
import com.colloquio.usermanagement.service.UserService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationRunner;

@ExtendWith(MockitoExtension.class)
class DatabaseStartupInitializerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private ResultSet resultSet;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Test
    void shouldImportMockCsvAtStartupWhenDatabaseIsEmpty() throws Exception {
        DatabaseStartupInitializer initializer = new DatabaseStartupInitializer();
        MockDataProperties properties = new MockDataProperties();
        properties.setEnabled(true);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getTables(null, null, "users", new String[]{"TABLE"})).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(userRepository.count()).thenReturn(0L);
        when(userService.importUsersFromCsvResource(any()))
                .thenReturn(new CsvImportResponse(3, 0, java.util.List.of()));

        ApplicationRunner runner = initializer.databaseStartupRunner(
                dataSource,
                userRepository,
                userService,
                properties
        );

        runner.run(null);

        verify(userService).importUsersFromCsvResource(any());
    }

    @Test
    void shouldNotImportMockCsvWhenMockDataIsDisabled() throws Exception {
        DatabaseStartupInitializer initializer = new DatabaseStartupInitializer();
        MockDataProperties properties = new MockDataProperties();
        properties.setEnabled(false);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getTables(null, null, "users", new String[]{"TABLE"})).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        ApplicationRunner runner = initializer.databaseStartupRunner(
                dataSource,
                userRepository,
                userService,
                properties
        );

        runner.run(null);

        verify(userRepository, never()).count();
        verify(userService, never()).importUsersFromCsvResource(any());
    }
}
