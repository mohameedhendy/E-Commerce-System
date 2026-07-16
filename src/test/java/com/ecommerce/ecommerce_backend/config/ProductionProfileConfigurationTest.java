package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
    public void productionProfileUsesSafeRuntimeSettings() {

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
        });
    }
}