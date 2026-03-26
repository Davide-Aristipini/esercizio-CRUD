package com.colloquio.usermanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mock-data")
public class MockDataProperties {

    private boolean enabled = true;
}
