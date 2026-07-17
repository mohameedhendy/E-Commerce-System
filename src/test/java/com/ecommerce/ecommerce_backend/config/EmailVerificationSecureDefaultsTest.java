package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EmailVerificationSecureDefaultsTest {

    private static final String PROPERTY_NAME =
            "app.email.verification.enabled";

    private static final String SECURE_DEFAULT =
            "${EMAIL_VERIFICATION_ENABLED:true}";

    @Test
    public void globalConfigurationDefaultsVerificationToEnabled()
            throws IOException {

        Properties properties =
                loadProperties(
                        "src/main/resources/application.properties"
                );

        Assertions.assertEquals(
                SECURE_DEFAULT,
                properties.getProperty(PROPERTY_NAME),
                "Global configuration must default email verification to enabled."
        );
    }

    @Test
    public void productionConfigurationDefaultsVerificationToEnabled()
            throws IOException {

        Properties properties =
                loadProperties(
                        "src/main/resources/application-prod.properties"
                );

        Assertions.assertEquals(
                SECURE_DEFAULT,
                properties.getProperty(PROPERTY_NAME),
                "Production configuration must explicitly default email verification to enabled."
        );
    }

    private Properties loadProperties(
            String sourcePath
    ) throws IOException {

        Properties properties =
                new Properties();

        try (
                Reader reader =
                        Files.newBufferedReader(
                                Path.of(sourcePath),
                                StandardCharsets.UTF_8
                        )
        ) {

            properties.load(reader);
        }

        return properties;
    }
}
