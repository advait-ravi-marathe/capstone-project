package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the actual versioned migrations against the configured embedded PostgreSQL binaries. */
class LocalSchemaMigrationInfrastructureTest {
    @Test
    void day59VersionedBootstrapIsRepeatableAndNonDestructive() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway flyway = Flyway.configure().dataSource(dataSource)
                    .locations("classpath:db/migration").target("3").load();
            assertEquals(3, flyway.migrate().migrationsExecuted);
            String historyBefore = count(dataSource, "SELECT count(*) FROM flyway_schema_history WHERE success") + ":"
                    + scalarRows(dataSource, "SELECT version || ':' || checksum || ':' || success FROM flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank");
            String rowsBefore = lifecycleRows(dataSource);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES ('day59-sentinel','owner-a','journey-sentinel','quote-sentinel','home-secure-v1','home-secure-rate-v1',9.25,'INR',DATE '2026-12-18','ISSUED',1)");
            }
            String withSentinel = lifecycleRows(dataSource);
            assertNotEquals(rowsBefore, withSentinel);
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(withSentinel, lifecycleRows(dataSource));
            String historyAfter = count(dataSource, "SELECT count(*) FROM flyway_schema_history WHERE success") + ":"
                    + scalarRows(dataSource, "SELECT version || ':' || checksum || ':' || success FROM flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank");
            assertEquals(historyBefore, historyAfter);
            assertTrue(hasPolicy(dataSource, "day59-sentinel"));
        }
    }

    @Test
    void versionedBootstrapIncludesEventLogDeliveryAndIsRepeatableAndNonDestructive() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();

            assertEquals(5, flyway.migrate().migrationsExecuted);
            assertEquals(3, countPolicies(dataSource));
            assertEquals(0, countRows(dataSource, "training_issuance_work"));
            assertEquals(0, countRows(dataSource, "training_issuance_delivery_receipt"));
            assertEquals(0, countRows(dataSource, "training_issuance_recovery_history"));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version)
                        SELECT policy_id,version,event_type,effective_date,1 FROM training_policy_lifecycle_event
                         WHERE event_type='ISSUED'
                        """);
            }
            assertEquals(3, count(dataSource, "SELECT count(*) FROM training_issuance_work WHERE control_revision=0 AND quarantined_at IS NULL"));
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
            assertEquals(3, count(dataSource, "SELECT count(*) FROM training_issuance_work WHERE control_revision=0 AND quarantined_at IS NULL"));
        }
    }

    private static int countRows(DataSource dataSource, String table) throws Exception {
        if (!table.matches("training_issuance_(work|delivery_receipt|recovery_history)")) throw new IllegalArgumentException("fixed table only");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM " + table)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static int countPolicies(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static int count(DataSource dataSource, String sql) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery(sql)) { assertTrue(rows.next()); return rows.getInt(1); }
    }

    private static boolean hasPolicy(DataSource dataSource, String policyId) throws Exception {
        try (Connection connection = dataSource.getConnection(); var statement = connection.prepareStatement(
                "SELECT 1 FROM training_policy_lifecycle WHERE policy_id = ?")) {
            statement.setString(1, policyId);
            try (var rows = statement.executeQuery()) { return rows.next(); }
        }
    }

    private static String scalarRows(DataSource dataSource, String sql) throws Exception {
        StringBuilder result = new StringBuilder();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery(sql)) {
            while (rows.next()) result.append(rows.getString(1)).append('\n');
        }
        return result.toString();
    }

    private static String lifecycleRows(DataSource dataSource) throws Exception {
        return table(dataSource, "training_policy_lifecycle", "policy_id") + "\n"
                + table(dataSource, "training_policy_lifecycle_event", "policy_id,version") + "\n"
                + table(dataSource, "training_policy_idempotency", "owner_id,client_request_id");
    }

    private static String table(DataSource dataSource, String name, String order) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + order
                     + "), '[]'::jsonb)::text FROM " + name + " t")) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }
}
