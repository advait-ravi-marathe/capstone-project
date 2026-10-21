package assignments.insurance;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
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
            }

            var store = new JdbcPolicyRenewalReadStore(dataSource);
            var found = store.findByPolicyIdAndOwnerId("renewal-demo", "owner-a");
            assertEquals(new PolicyRenewalFacts("renewal-demo", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED,
                    3, "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR"), found.orElseThrow());
            assertTrue(store.findByPolicyIdAndOwnerId("renewal-demo", "owner-b").isEmpty(), "foreign policy is concealed");
            assertTrue(store.findByPolicyIdAndOwnerId("missing", "owner-a").isEmpty());
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement();
                 var rows = s.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1), "preview lookup is read-only");
            }
        }
    }
}
