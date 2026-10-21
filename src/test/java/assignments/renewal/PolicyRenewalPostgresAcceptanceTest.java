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
import java.sql.ResultSet;
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
            var before = allRows(dataSource);
            var store = new JdbcPolicyRenewalReadStore(dataSource);
            var controller = new PolicyRenewalPreviewController(store, new PolicyRenewalPreviewPolicy(),
                    Clock.fixed(Instant.parse("2027-11-15T12:00:00Z"), ZoneOffset.UTC));
            MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

            var own = mvc.perform(get("/api/policies/renewal-demo/renewal-preview").principal(() -> "owner-a"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertApprovedPreview(own, "renewal-demo", 3, "home-secure-v1", "home-secure-rate-v1", "125.00", "INR", "2027-12-01");
            assertEquals(before, allRows(dataSource), "own GET leaves complete policy/event/receipt rows unchanged");
            var foreignResult = mvc.perform(get("/api/policies/renewal-demo/renewal-preview").principal(() -> "owner-b"))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"))
                    .andReturn().getResponse().getContentAsString();
            assertEquals(before, allRows(dataSource), "foreign GET leaves complete rows unchanged");
            var absent = mvc.perform(get("/api/policies/missing/renewal-preview").principal(() -> "owner-a"))
                    .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
            assertEquals(foreignResult, absent, "foreign and absent cases are indistinguishable");
            assertEquals(before, allRows(dataSource), "absent GET leaves complete rows unchanged");
            mvc.perform(get("/api/policies/renewal-demo/renewal-preview"))
                    .andExpect(status().isUnauthorized()).andExpect(content().json("{\"code\":\"UNAUTHENTICATED\"}", true));
            assertEquals(before, allRows(dataSource), "anonymous request performs no write");
            assertEquals("{\"code\":\"POLICY_NOT_FOUND\"}", foreignResult);
        }
    }

    private static void seed(javax.sql.DataSource dataSource) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                if (!sql.isBlank()) s.execute(sql);
            s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-demo','owner-a','journey-hidden','quote-hidden','home-secure-v1','home-secure-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',3,NULL)");
            s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('renewal-demo',3,'ISSUED',DATE '2026-12-01',NULL,NULL)");
            s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','renewal-request','opaque-original','renewal-demo')");
        }
    }

    static java.util.Map<String, String> allRows(javax.sql.DataSource dataSource) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            return java.util.Map.of("policy", jsonRows(s, "training_policy_lifecycle", "policy_id"),
                    "events", jsonRows(s, "training_policy_lifecycle_event", "policy_id, version"),
                    "receipts", jsonRows(s, "training_policy_idempotency", "owner_id, client_request_id"));
        }
    }

    static String jsonRows(Statement statement, String table, String ordering) throws Exception {
        try (ResultSet rows = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + ordering +
                "), '[]'::jsonb)::text FROM " + table + " t")) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }

    private static void assertApprovedPreview(String content, String id, int version, String product, String rate,
                                              String premium, String currency, String date) throws Exception {
        var json = new tools.jackson.databind.ObjectMapper().readTree(content);
        assertEquals(java.util.Set.of("policyId", "sourcePolicyVersion", "productVersion", "rateVersion",
                "currentAnnualPremiumRupees", "currency", "nextTermEffectiveDate", "notice"), jsonFieldNames(json));
        assertTrue(json.get("policyId").isTextual());
        assertEquals(id, json.get("policyId").asString());
        assertTrue(json.get("sourcePolicyVersion").isIntegralNumber());
        assertEquals((long) version, json.get("sourcePolicyVersion").asLong());
        assertTrue(json.get("productVersion").isTextual());
        assertEquals(product, json.get("productVersion").asString());
        assertTrue(json.get("rateVersion").isTextual());
        assertEquals(rate, json.get("rateVersion").asString());
        assertTrue(json.get("currentAnnualPremiumRupees").isNumber());
        assertEquals(0, new BigDecimal(premium).compareTo(json.get("currentAnnualPremiumRupees").decimalValue()));
        assertTrue(json.get("currency").isTextual());
        assertEquals(currency, json.get("currency").asString());
        assertTrue(json.get("nextTermEffectiveDate").isTextual());
        assertEquals(date, json.get("nextTermEffectiveDate").asString());
        assertTrue(json.get("notice").isTextual());
        assertEquals("Reference only; not a renewal offer or coverage commitment.", json.get("notice").asString());
        assertFalse(content.contains("journey-hidden"));
        assertFalse(content.contains("quote-hidden"));
    }

    private static java.util.Set<String> jsonFieldNames(tools.jackson.databind.JsonNode json) {
        var fields = new java.util.HashSet<String>(); fields.addAll(json.propertyNames()); return fields;
    }
}
