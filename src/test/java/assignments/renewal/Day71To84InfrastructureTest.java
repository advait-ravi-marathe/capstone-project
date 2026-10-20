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
        LocalDate start = LocalDate.parse("2026-12-29");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.get("days").get(i);
            JsonNode card = stories.get("cards").get(i);
            assertEquals(71 + i, day.get("day").asInt());
            assertEquals(start.plusDays(i).toString(), day.get("date").asString());
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertEquals(180, day.get("engineering").get("budget_minutes").asInt()
                    + day.get("java_dsa").get("budget_minutes").asInt());
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
            assertEquals(180, card.get("budget_minutes").asInt());
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
        for (String file : new String[]{"manifest.json", "continuation-31-44/manifest.json",
                "continuation-45-56/manifest.json", "continuation-57-70/manifest.json"}) {
            Path path = file.equals("manifest.json") ? BASE.resolve(file) : Path.of("docs/reboot").resolve(file);
            for (JsonNode day : new ObjectMapper().readTree(Files.readString(path)).get("days")) {
                JsonNode dsa = day.get("java_dsa");
                if (dsa != null && "new_pattern_anchor".equals(dsa.path("mode").asString()))
                    assertTrue(ids.add(dsa.get("problem_id").asInt()), "duplicate DSA anchor " + dsa.get("problem_id"));
            }
        }
        assertTrue(ids.contains(112));
        assertTrue(ids.contains(230));
        assertTrue(Files.readString(Path.of("src/main/resources/application-local.properties"))
                .contains("spring.datasource.hikari.connection-timeout=1000"));
    }
}
