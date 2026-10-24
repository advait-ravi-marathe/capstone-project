package assignments.reboot;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor checks for manifest integrity and the isolated, synthetic SQL harness. */
class ContinuationManifestInfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-31-44");

    @Test void fourteenContractsAreDatedBudgetedAndNotMarkedComplete() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode cards = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.get("days").size());
        assertEquals(14, cards.get("cards").size());
        assertEquals("https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0",
                manifest.get("separate_learning_repository").get("algorithm_practice").asString());
        LocalDate start = LocalDate.parse("2026-11-23");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.get("days").get(i);
            JsonNode card = cards.get("cards").get(i);
            assertEquals(31 + i, day.get("day").asInt());
            assertEquals(start.plusDays(i).toString(), day.get("date").asString());
            assertEquals(day.get("day").asInt(), card.get("day_number").asInt());
            assertEquals(day.get("date").asString(), card.get("date").asString());
            assertEquals(day.get("engineering"), card.get("engineering"));
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertEquals("blocked", day.get("integration_status").asString());
            assertEquals(120, day.get("engineering").get("budget_minutes").asInt());
            assertEquals(85, day.get("engineering").get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, day.get("engineering").get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, day.get("engineering").get("allocation_minutes").get("evidence").asInt());
            assertTrue(Files.isRegularFile(Path.of(day.get("contract_file").asString())));
            assertTrue(Files.isRegularFile(Path.of(day.get("engineering").get("acceptance_test_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(day.get("engineering").get("evidence_reference").asString())));
        }
        assertEquals(12, cards.get("sprint_boundaries").get(0).get("batch_story_count").asInt());
        assertEquals(2, cards.get("sprint_boundaries").get(1).get("batch_story_count").asInt());
    }

    @Test void isolatedTrainingSchemaEnforcesLifecycleUniquenessAndNoCustomerPiiColumns() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
                for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                    if (!sql.isBlank()) statement.execute(sql);
                try (var columns = connection.getMetaData().getColumns(null, "public", "training_policy_lifecycle", null)) {
                    while (columns.next()) assertFalse(Set.of("full_name", "address", "email", "phone", "birth_date").contains(columns.getString("COLUMN_NAME")));
                }
                statement.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('p-1','owner-demo','journey-demo','quote-demo','home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1,NULL)");
                statement.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-demo','request-demo','fingerprint-demo','p-1')");
                assertThrows(SQLException.class, () -> statement.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-demo','request-demo','other-fingerprint','p-1')"));
                assertThrows(SQLException.class, () -> statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('missing-policy',1,'ISSUED',DATE '2026-12-01',NULL,NULL)"));
                try (var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) { rows.next(); assertEquals(1, rows.getInt(1)); }
            }
        }
    }
}
