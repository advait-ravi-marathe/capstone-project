package assignments.insurance;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Day 48 exercises the actual JDBC projection against isolated PostgreSQL. */
@Tag("learnerAcceptance")
class PolicyRenewalRepositoryAcceptanceTest {
    @Test void day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
                for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                    if (!sql.isBlank()) s.execute(sql);
                s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-demo','owner-a','journey-hidden','quote-hidden','home-secure-v1','home-secure-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',3,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-other','owner-b','journey-b','quote-b','home-secure-v2','home-secure-rate-v2',987.40,'INR',DATE '2027-04-17','ISSUED',9,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('renewal-demo',3,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('renewal-other',9,'ISSUED',DATE '2027-04-17',NULL,NULL)");
                s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','request-a','opaque:one','renewal-demo')");
                s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-b','request-b','opaque:two','renewal-other')");
            }

            var store = new JdbcPolicyRenewalReadStore(dataSource);
            var before = allRows(dataSource);
            var found = store.findByPolicyIdAndOwnerId("renewal-demo", "owner-a");
            assertEquals(before, allRows(dataSource), "own lookup preserves all rows immediately");
            assertEquals(new PolicyRenewalFacts("renewal-demo", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED,
                    3, "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR"), found.orElseThrow());
            assertTrue(store.findByPolicyIdAndOwnerId("renewal-demo", "owner-b").isEmpty(), "foreign policy is concealed");
            assertEquals(before, allRows(dataSource), "foreign lookup preserves all rows immediately");
            assertTrue(store.findByPolicyIdAndOwnerId("missing", "owner-a").isEmpty());
            assertEquals(before, allRows(dataSource), "missing lookup preserves all rows immediately");
            assertTrue(store.findByPolicyIdAndOwnerId("renewal-other", "owner-a").isEmpty());
            assertEquals(before, allRows(dataSource), "second foreign lookup preserves all rows immediately");
            assertEquals(new PolicyRenewalFacts("renewal-other", LocalDate.parse("2027-04-17"), PolicyStatus.ISSUED,
                    9, "home-secure-v2", "home-secure-rate-v2", new BigDecimal("987.40"), "INR"),
                    store.findByPolicyIdAndOwnerId("renewal-other", "owner-b").orElseThrow());
            assertEquals(before, allRows(dataSource), "second-owner lookup preserves all rows immediately");
            assertTrue(store.findByPolicyIdAndOwnerId("renewal-demo' OR '1'='1", "owner-a").isEmpty(),
                    "malicious-looking identifiers remain parameter data");
            assertEquals(before, allRows(dataSource), "SQL-looking value preserves all rows immediately");
            assertEquals(before, allRows(dataSource), "the complete policy, event and receipt rows remain unchanged");
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement();
                 var rows = s.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
                assertTrue(rows.next());
                assertEquals(2, rows.getInt(1), "both complete policy fixtures remain present");
            }
        }
    }

    private static java.util.Map<String, String> allRows(javax.sql.DataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            return java.util.Map.of("policy", jsonRows(s, "training_policy_lifecycle", "policy_id"),
                    "events", jsonRows(s, "training_policy_lifecycle_event", "policy_id, version"),
                    "receipts", jsonRows(s, "training_policy_idempotency", "owner_id, client_request_id"));
        }
    }

    private static String jsonRows(Statement statement, String table, String ordering) throws Exception {
        try (ResultSet rows = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + ordering +
                "), '[]'::jsonb)::text FROM " + table + " t")) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }
}
