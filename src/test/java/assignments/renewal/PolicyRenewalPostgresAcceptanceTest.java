package assignments.renewal;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Days 54–55 use real PostgreSQL and a real Spring MVC mapping; red at fixed learner seams. */
@Tag("learnerAcceptance")
class PolicyRenewalPostgresAcceptanceTest {
    @Test void day54OwnerScopedEndpointReadsPostgresAndLeavesPolicyRowsUnchanged() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            seed(dataSource);
            var store = new JdbcPolicyRenewalReadStore(dataSource);
            var controller = new PolicyRenewalPreviewController(store, new PolicyRenewalPreviewPolicy(),
                    Clock.fixed(Instant.parse("2027-11-15T12:00:00Z"), ZoneOffset.UTC));
            MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

            mvc.perform(get("/api/policies/renewal-demo/renewal-preview").principal(() -> "owner-a"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.policyId").value("renewal-demo"))
                    .andExpect(jsonPath("$.nextTermEffectiveDate").value("2027-12-01"));
            mvc.perform(get("/api/policies/renewal-demo/renewal-preview").principal(() -> "owner-b"))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
            assertEquals(1, count(dataSource), "both preview reads are side-effect free");
        }
    }

    private static void seed(javax.sql.DataSource dataSource) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                if (!sql.isBlank()) s.execute(sql);
            s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-demo','owner-a','journey-hidden','quote-hidden','home-secure-v1','home-secure-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',3,NULL)");
        }
    }

    private static long count(javax.sql.DataSource dataSource) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement();
             var rows = s.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next()); return rows.getLong(1);
        }
    }
}
