package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        properties = {
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.jpa.defer-datasource-initialization=false",
                "spring.sql.init.mode=never",
                "spring.flyway.enabled=true",
                "spring.flyway.baseline-on-migrate=false",
                "spring.flyway.validate-on-migrate=true",
                "app.email.verification.enabled=false",
                "app.security.auth-rate-limit.enabled=false"
        }
)
class PostgreSqlMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>(
                    DockerImageName.parse(
                            "postgres:17-alpine"
                    )
            )
                    .withDatabaseName(
                            "ecommerce_migration_test"
                    )
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayAppliesAllMigrationsOnFreshPostgreSqlDatabase() {

        Integer successfulMigration =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '18'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        Integer failedMigrations =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE success = FALSE
                        """,
                        Integer.class
                );

        List<String> requiredTables =
                jdbcTemplate.queryForList(
                        """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                              'local_user',
                              'product',
                              'stock',
                              'web_order',
                              'product_order_quantity',
                              'review',
                              'shopping_cart',
                              'shopping_cart_item',
                              'refresh_session'
                          )
                        ORDER BY table_name
                        """,
                        String.class
                );

        assertThat(successfulMigration)
                .isEqualTo(1);

        assertThat(failedMigrations)
                .isZero();

        assertThat(requiredTables)
                .containsExactlyInAnyOrder(
                        "local_user",
                        "product",
                        "stock",
                        "web_order",
                        "product_order_quantity",
                        "review",
                        "shopping_cart",
                        "shopping_cart_item",
                        "refresh_session"
                );
    }

    @Test
    void reviewIntegrityConstraintsExistInPostgreSql() {

        List<String> constraints =
                jdbcTemplate.queryForList(
                        """
                        SELECT conname
                        FROM pg_constraint
                        WHERE conrelid = 'public.review'::regclass
                        """,
                        String.class
                );

        List<String> indexes =
                jdbcTemplate.queryForList(
                        """
                        SELECT indexname
                        FROM pg_indexes
                        WHERE schemaname = 'public'
                          AND tablename = 'review'
                        """,
                        String.class
                );

        Map<String, String> nullability =
                jdbcTemplate.query(
                        """
                        SELECT column_name, is_nullable
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'review'
                          AND column_name IN (
                              'rating',
                              'comment',
                              'created_at'
                          )
                        """,
                        resultSet -> {

                            java.util.HashMap<String, String>
                                    result =
                                    new java.util.HashMap<>();

                            while (resultSet.next()) {
                                result.put(
                                        resultSet.getString(
                                                "column_name"
                                        ),
                                        resultSet.getString(
                                                "is_nullable"
                                        )
                                );
                            }

                            return result;
                        }
                );

        assertThat(constraints)
                .contains(
                        "ck_review_rating",
                        "uk_review_user_product"
                );

        assertThat(indexes)
                .contains(
                        "idx_review_product_created_at"
                );

        assertThat(nullability)
                .containsEntry("rating", "NO")
                .containsEntry("comment", "NO")
                .containsEntry("created_at", "NO");
    }
}