package assignments.local;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor integrity checks; these do not claim learner application behavior is complete. */
class LocalApplicationManifestInfrastructureTest {
    private static final Path ROOT = Path.of("docs/reboot/continuation-57-70");

    @Test
    void fourteenContractsStayDatedBudgetedAndLearnerOwned() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(ROOT.resolve("manifest.json")));
        JsonNode scrum = new ObjectMapper().readTree(Files.readString(ROOT.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.get("days").size());
        assertEquals(14, scrum.get("cards").size());
        LocalDate start = LocalDate.parse("2026-12-21");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.get("days").get(i);
            assertEquals(57 + i, day.get("day").asInt());
            assertEquals(start.plusDays(i).toString(), day.get("date").asString());
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertTrue(day.get("scaffold_status").asString().equals("partial")
                            || day.get("scaffold_status").asString().equals("instructor_scaffold_complete"),
                    "scaffold status must distinguish instructor setup from learner mastery for Day " + (57 + i));
            assertFalse(day.get("engineering").get("learner_task").asString().isBlank());
            assertEquals("blocked until prior evidence gate and actual learner selectors pass",
                    day.get("integration_status").asString());
            assertTrue(Files.isRegularFile(Path.of(day.get("contract_file").asString())));
            assertTrue(Files.isRegularFile(Path.of(day.get("engineering").get("evidence_reference").asString())));
            JsonNode engineering = day.get("engineering");
            assertEquals(120, engineering.get("budget_minutes").asInt());
            assertEquals(85, engineering.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, engineering.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, engineering.get("allocation_minutes").get("evidence").asInt());
        }
    }

    @Test
    void localBindGuardAllowsLoopbackAndRejectsWildcardAndRemoteAddresses() throws Exception {
        LocalBindAddressGuard.requireLoopback("127.0.0.1");
        LocalBindAddressGuard.requireLoopback("::1");
        for (String address : new String[]{"0.0.0.0", "192.0.2.10", "localhost.invalid"}) {
            assertThrows(IllegalArgumentException.class, () -> LocalBindAddressGuard.requireLoopback(address));
        }
        assertTrue(InetAddress.getByName("127.0.0.1").isLoopbackAddress());
    }

    @Test
    void day70EvidenceAndDesignMockStayWithinTheDefinedSlice() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(ROOT.resolve("manifest.json")));
        assertTrue(manifest.get("scope").asString().contains("loopback-only"));
        assertTrue(manifest.get("scope").asString().contains("no real data"));
        assertEquals("not_attempted", manifest.get("days").get(13).get("learner_status").asString());
        assertTrue(Files.isRegularFile(Path.of(manifest.get("days").get(13)
                .get("engineering").get("evidence_reference").asString())));
    }
}
