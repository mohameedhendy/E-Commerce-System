package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ProductionProfileConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withInitializer(
                            new ConfigDataApplicationContextInitializer()
                    )
                    .withPropertyValues(
                            "spring.profiles.active=prod"
                    );

    @Test
    public void productionProfileUsesSafeRuntimeSettings()
            throws IOException {

        Properties applicationProperties =
                new Properties();

        try (
                InputStream inputStream =
                        Files.newInputStream(
                                Path.of(
                                        "src/main/resources/"
                                                + "application.properties"
                                )
                        )
        ) {
            applicationProperties.load(
                    inputStream
            );
        }

        Assertions.assertEquals(
                "${JWT_SECRET}",
                applicationProperties.getProperty(
                        "jwt.algorithm.key"
                ),
                "The main configuration must obtain "
                        + "the JWT secret from JWT_SECRET "
                        + "without a hardcoded fallback."
        );

        contextRunner.run(context -> {

            Assertions.assertEquals(
                    "validate",
                    context.getEnvironment().getProperty(
                            "spring.jpa.hibernate.ddl-auto"
                    )
            );

            Assertions.assertEquals(
                    "false",
                    context.getEnvironment().getProperty(
                            "spring.jpa.show-sql"
                    )
            );

            Assertions.assertEquals(
                    "graceful",
                    context.getEnvironment().getProperty(
                            "server.shutdown"
                    )
            );

            Assertions.assertEquals(
                    "30s",
                    context.getEnvironment().getProperty(
                            "spring.lifecycle.timeout-per-shutdown-phase"
                    )
            );

            Assertions.assertEquals(
                    "never",
                    context.getEnvironment().getProperty(
                            "server.error.include-stacktrace"
                    )
            );
            Assertions.assertEquals(
                    "false",
                    context.getEnvironment().getProperty(
                            "springdoc.api-docs.enabled"
                    )
            );

            Assertions.assertEquals(
                    "false",
                    context.getEnvironment().getProperty(
                            "springdoc.swagger-ui.enabled"
                    )
            );

            Assertions.assertEquals(
                    "default-src 'none'; "
                            + "frame-ancestors 'none'",
                    context.getEnvironment().getProperty(
                            "app.security.content-security-policy"
                    )
            );
            Assertions.assertEquals(
                    "default-src 'none'; "
                            + "frame-ancestors 'none'",
                    context.getEnvironment().getProperty(
                            "app.security."
                                    + "swagger-content-security-policy"
                    )
            );
        });
    }
}