package assignments.renewal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Day71To84InfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-71-84");

    @Test void fourteenDatedContractsRemainBudgetedInProgressAndLinkable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode stories = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals("db2caf3adabf11e6d619ab9a7f7e1f5aa40f2a24", manifest.get("base_sha").asString());
        assertEquals(14, manifest.get("days").size());
        assertEquals(14, stories.get("story_count").asInt());
        assertTrue(Files.isRegularFile(Path.of(manifest.get("dependency_review_path").asString())));
        assertTrue(manifest.get("scope").asString().contains("Reuse the completed Day48 owner-scoped SQL"));
        LocalDate start = LocalDate.parse("2027-01-02");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.get("days").get(i);
            JsonNode card = stories.get("cards").get(i);
            assertEquals(71 + i, day.get("day").asInt());
            assertEquals(start.plusDays(i).toString(), day.get("date").asString());
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertEquals(120, day.get("engineering").get("budget_minutes").asInt());
            assertTrue(day.get("java_dsa").get("budget_minutes").isNull());
            assertEquals(60, day.get("java_dsa").get("budget_minutes_by_exercise_count").get("1_or_2_substantive_exercises").asInt());
            assertEquals(75, day.get("java_dsa").get("budget_minutes_by_exercise_count").get("3_substantive_exercises").asInt());
            assertEquals(90, day.get("java_dsa").get("budget_minutes_by_exercise_count").get("4_substantive_exercises").asInt());
            assertEquals(180, manifest.get("daily_budget_minutes").get("total_by_dsa_tier_minutes").get("1_or_2").asInt());
            assertEquals(195, manifest.get("daily_budget_minutes").get("total_by_dsa_tier_minutes").get("3").asInt());
            assertEquals(210, manifest.get("daily_budget_minutes").get("total_by_dsa_tier_minutes").get("4").asInt());
            assertEquals(85, day.get("engineering").get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, day.get("engineering").get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, day.get("engineering").get("allocation_minutes").get("evidence").asInt());
            for (JsonNode reference : new JsonNode[]{day.get("contract_file"),
                    day.get("engineering").get("evidence_reference"),
                    day.get("engineering").get("acceptance_test_path"),
                    day.get("engineering").get("focused_acceptance_test_path"),
                    day.get("engineering").get("infrastructure_test_path"),
                    day.get("java_dsa").get("guide_path")}) {
                if (reference == null || reference.isNull()) continue;
                String path = reference.asString();
                assertTrue(Files.isRegularFile(Path.of(path)), "missing " + path);
            }
            if (day.get("day").asInt() == 83) {
                assertFalse(day.get("engineering").get("acceptance_commands").isEmpty());
                assertTrue(day.get("engineering").get("acceptance_selector_status").asString()
                        .startsWith("installed_"));
            }
            assertEquals(day.get("date").asString(), card.get("date").asString());
            assertTrue(card.get("budget_minutes").isNull());
            assertEquals(180, card.get("budget_minutes_min").asInt());
            assertEquals(210, card.get("budget_minutes_max").asInt());
            JsonNode source = day.get("engineering").get("concept_course").get("resources").get(0);
            assertEquals("https", URI.create(source.get("url").asString()).getScheme());
        }
    }

    @Test void fixedBoundsAndDsaAnchorsAreSafeAndUnique() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        assertEquals(2, manifest.get("capacity_decision").get("application_permits").asInt());
        assertEquals(4, manifest.get("capacity_decision").get("existing_hikari_maximum_pool_size").asInt());
        assertEquals(1000, manifest.get("capacity_decision").get("connection_timeout_millis").asInt());
        Set<Integer> ids = new HashSet<>();
        for (JsonNode anchor : new ObjectMapper().readTree(Files.readString(
                Path.of("docs/reboot/dsa/canonical-anchors.json"))).get("anchors"))
            assertTrue(ids.add(anchor.get("problem_id").asInt()), "duplicate canonical anchor " + anchor.get("problem_id"));
        assertTrue(ids.contains(112));
        assertTrue(ids.contains(230));
        assertTrue(Files.readString(Path.of("src/main/resources/application-local.properties"))
                .contains("spring.datasource.hikari.connection-timeout=1000"));
    }
}
