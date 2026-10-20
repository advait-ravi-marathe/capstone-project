package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the actual versioned migrations against the configured embedded PostgreSQL binaries. */
class LocalSchemaMigrationInfrastructureTest {
    @Test
    void day59VersionedBootstrapIsRepeatableAndNonDestructive() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();

            assertEquals(3, flyway.migrate().migrationsExecuted);
            assertEquals(3, countPolicies(dataSource));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO training_policy_lifecycle
                          (policy_id, owner_id, journey_id, quote_version_id, product_version, rate_version,
                           annual_premium_rupees, currency, effective_date, status, version)
                        VALUES ('policy-sentinel', 'owner-a', 'journey-sentinel', 'quote-sentinel',
                                'home-secure-v1', 'home-secure-rate-v1', 1.00, 'INR',
                                DATE '2026-01-01', 'ISSUED', 1)
                        """);
            }

            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(4, countPolicies(dataSource));
            assertTrue(hasPolicy(dataSource, "policy-sentinel"));
        }
    }

    private static int countPolicies(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static boolean hasPolicy(DataSource dataSource, String policyId) throws Exception {
        try (Connection connection = dataSource.getConnection(); var statement = connection.prepareStatement(
                "SELECT 1 FROM training_policy_lifecycle WHERE policy_id = ?")) {
            statement.setString(1, policyId);
            try (var rows = statement.executeQuery()) { return rows.next(); }
        }
    }
}
