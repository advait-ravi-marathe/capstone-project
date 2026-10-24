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
            assertEquals(85, day.get("engineering").get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, day.get("engineering").get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, day.get("engineering").get("allocation_minutes").get("evidence").asInt());
            for (JsonNode reference : new JsonNode[]{day.get("contract_file"),
                    day.get("engineering").get("evidence_reference"),
                    day.get("engineering").get("acceptance_test_path"),
                    day.get("engineering").get("focused_acceptance_test_path"),
                    day.get("engineering").get("infrastructure_test_path")}) {
                if (reference == null || reference.isNull()) continue;
                assertTrue(Files.isRegularFile(Path.of(reference.asString())), "missing " + reference.asString());
            }
            if (day.get("day").asInt() == 83) {
                assertFalse(day.get("engineering").get("acceptance_commands").isEmpty());
                assertTrue(day.get("engineering").get("acceptance_selector_status").asString()
                        .startsWith("installed_"));
            }
            assertEquals(day.get("date").asString(), card.get("date").asString());
            assertFalse(card.has("budget_minutes_min"));
            assertFalse(card.has("budget_minutes_max"));
            JsonNode source = day.get("engineering").get("concept_course").get("resources").get(0);
            assertEquals("https", URI.create(source.get("url").asString()).getScheme());
        }
    }

    @Test void fixedCapacityBoundsAreSafe() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        assertEquals(2, manifest.get("capacity_decision").get("application_permits").asInt());
        assertEquals(4, manifest.get("capacity_decision").get("existing_hikari_maximum_pool_size").asInt());
        assertEquals(1000, manifest.get("capacity_decision").get("connection_timeout_millis").asInt());
        assertTrue(Files.readString(Path.of("src/main/resources/application-local.properties"))
                .contains("spring.datasource.hikari.connection-timeout=1000"));
    }

    @Test void day79AndDay83UseTheBoundedSprint6RuntimeOnly() throws Exception {
        String entry = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalApplication.java"));
        String config = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalConfiguration.java"));
        String sharedStore = Files.readString(Path.of("src/main/java/assignments/local/LocalPolicyRenewalReadStoreConfiguration.java"));
        String localRuntime = Files.readString(Path.of("src/main/java/assignments/local/LocalRuntimeConfiguration.java"));
        String helper = Files.readString(Path.of("src/test/java/assignments/local/Sprint6PackagedRuntimeProcess.java"));
        String browser = Files.readString(Path.of("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java"));
        String browserScript = Files.readString(Path.of("frontend/e2e/day83-lock-recovery.mjs"));
        String wiringTest = Files.readString(Path.of("src/test/java/assignments/renewal/PreviewRuntimeWiringAcceptanceTest.java"));
        String pom = Files.readString(Path.of("pom.xml"));
        String packageScript = Files.readString(Path.of("scripts/package-sprint6-demo.sh"));
        String sprint6Properties = Files.readString(Path.of("src/main/resources/application-sprint6-local.properties"));
        String security = Files.readString(Path.of("src/main/java/assignments/local/LocalSecurityConfiguration.java"));
        String ownerExport = Files.readString(BASE.resolve("scrum-owner-manifest.json"));

        assertTrue(entry.contains("@Import(Sprint6LocalConfiguration.class)"));
        assertTrue(config.contains("LocalApplicationComposition.compose(sprint6Clock, dataSource)"));
        assertTrue(config.contains("LocalPolicyRenewalReadStoreConfiguration.class"));
        assertFalse(config.contains("LEARNER TODO Day 79"));
        assertTrue(Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"))
                .contains("scanBasePackages = \"assignments.local\""));
        assertFalse(localRuntime.contains("LocalPolicyRenewalReadStoreConfiguration.class"));
        assertFalse(localRuntime.contains("LEARNER TODO Day 79"));
        assertEquals(1, occurrences(sharedStore, "LEARNER TODO Day 79"));
        assertFalse(config.contains("PreviewReadBudgetPolicy"));
        assertFalse(config.contains("TimedPolicyRenewalReadStore"));
        assertFalse(config.contains("LocalRuntimeConfiguration.class"));
        assertTrue(wiringTest.contains("assertInstanceOf(IllegalArgumentException.class, root,"),
                "invalid capacity must fail for its validation cause, not any context startup failure");
        assertTrue(pom.contains("<id>sprint6-local</id>"));
        assertTrue(pom.contains("assignments.sprint6.Sprint6LocalApplication"));
        assertTrue(packageScript.contains("-Plocal-app,sprint6-local"));
        assertTrue(packageScript.contains("sourceContentSha256"));
        assertTrue(helper.contains("capstone-starter-1.0-SNAPSHOT-sprint6.jar"));
        assertTrue(helper.contains("--spring.profiles.active=local,sprint6-local"));
        assertTrue(helper.contains("verifyArtifactReceipt(jar, evidence)"));
        assertTrue(browser.contains("Sprint6PackagedRuntimeProcess.start(postgres, CLOCK)"));
        assertFalse(browser.contains("LocalPackagedRuntimeProcess.start"));
        assertTrue(browserScript.contains("/actuator/metrics/hikaricp.connections.active"),
                "Day 83 exercises the read-only Hikari metric through the bounded packaged runtime");
        assertTrue(browserScript.contains("/actuator/metrics/coverstory.preview.requests"),
                "Day 83 exercises the read-only route metric through the bounded packaged runtime");
        assertTrue(sprint6Properties.contains("management.endpoints.access.default=none"));
        assertTrue(sprint6Properties.contains("management.endpoint.metrics.access=read-only"));
        assertFalse(sprint6Properties.contains("include=*"), "the isolated profile must not expose all actuator endpoints");
        assertTrue(sprint6Properties.contains("management.endpoints.web.exposure.include=health,metrics"),
                "only health and the required metrics endpoint are exposed in the synthetic loopback profile");
        assertTrue(security.contains(".anyRequest().authenticated()"),
                "exposed metrics remain behind the local synthetic-session security chain");
        assertFalse(security.contains("/actuator/metrics"), "metrics must not be added to the public allow-list");

        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode day79 = manifest.get("days").get(8).get("engineering");
        JsonNode day83 = manifest.get("days").get(12).get("engineering");
        assertEquals("LocalPolicyRenewalReadStoreConfiguration.policyRenewalReadStore", day79.get("learner_seam").asString());
        assertTrue(day83.get("acceptance_commands").toString().contains("package-sprint6-demo.sh"));
        assertTrue(Files.readString(Path.of("docs/reboot/continuation-71-84/contracts/day-83.md"))
                .contains("sprint6-local` classifier JAR and receipt"));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int at = 0; (at = text.indexOf(needle, at)) >= 0; at += needle.length()) count++;
        return count;
    }

    @Test void suppliedArchitectureAndInterviewAssignmentsAreCompleteAndSynchronized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode stories = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        String ownerExport = Files.readString(BASE.resolve("scrum-owner-manifest.json"));
        String questionBank = Files.readString(Path.of("docs/reboot/interview/question-bank.md"));
        String notes = Files.readString(BASE.resolve("assistant-architecture-notes.md"));
        Set<Integer> expectedMockDays = Set.of(73, 76, 80, 81);
        Set<Integer> actualMockDays = new HashSet<>();
        int firstWeekMocks = 0;
        int secondWeekMocks = 0;

        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.path("days").get(i);
            JsonNode card = stories.path("cards").get(i);
            int number = 71 + i;
            String notePath = day.path("engineering").path("assistant_architecture_note").asText();
            assertEquals(notePath, card.path("engineering").path("assistant_architecture_note").asText());
            assertTrue(notePath.endsWith("assistant-architecture-notes.md#day-" + number));
            assertTrue(notes.contains("<a id=\"day-" + number + "\"></a>"));
            JsonNode studyAllocation = day.path("engineering").path("concept_course").path("allocation_minutes");
            assertEquals(3, studyAllocation.size(), "study categories must stay bounded to the agreed 25-minute block");
            assertEquals(10, studyAllocation.path("assistant_architecture_note").asInt());
            assertEquals(25, studyAllocation.path("focused_reading").asInt()
                    + studyAllocation.path("assistant_architecture_note").asInt()
                    + studyAllocation.path("retrieval_check").asInt());
            assertFalse(studyAllocation.has("design_sketch"), "capstone proposal/sketch work is not assigned");
            Path contractPath = Path.of(day.path("contract_file").asText());
            String contract = Files.readString(contractPath);
            assertTrue(contract.length() > 700, "day " + number + " must have a substantive contract");
            assertTrue(contract.contains("Assistant-provided architecture note"), "day " + number);
            assertFalse(contract.contains("**Design sketch:**"), "day " + number);
            assertTrue(day.path("course_learning").path("videos_watched").asBoolean() == false,
                    "course metadata is not evidence of viewing for day " + number);

            JsonNode canonicalAssignment = day.path("engineering").path("interview_assignment");
            JsonNode ownerAssignment = card.path("engineering").path("interview_assignment");
            if (expectedMockDays.contains(number)) {
                actualMockDays.add(number);
                assertEquals(canonicalAssignment, ownerAssignment, "mock assignment sync for day " + number);
                assertEquals("45_minute_mock", canonicalAssignment.path("mode").asText());
                assertEquals(45, canonicalAssignment.path("duration_minutes").asInt());
                assertEquals(85, canonicalAssignment.path("practice_total_minutes").asInt());
                assertEquals(45, canonicalAssignment.path("mock_minutes").asInt());
                assertEquals(40, canonicalAssignment.path("eai_slice_minutes").path("preconditions_and_prediction").asInt()
                        + canonicalAssignment.path("eai_slice_minutes").path("implementation_or_observation").asInt()
                        + canonicalAssignment.path("eai_slice_minutes").path("selector_and_blocker_record").asInt());
                assertEquals("system_design_interview_question_only", canonicalAssignment.path("proposal_scope").asText());
                assertEquals("assistant_supplied", canonicalAssignment.path("capstone_architecture").asText());
                assertTrue(contract.contains("interview question only"));
                assertTrue(contract.contains(canonicalAssignment.path("deferred_work_id").asText()));
                assertTrue(Files.isRegularFile(Path.of(day.path("engineering").path("acceptance_test_path").asText())),
                        "missing learner selector for day " + number);
                assertTrue(questionBank.contains("<a id=\"" + canonicalAssignment.path("question_id").asText().toLowerCase() + "\"></a>"));
                int phases = 0;
                for (JsonNode phase : canonicalAssignment.path("mock_phases")) phases += phase.path("minutes").asInt();
                assertEquals(45, phases);
                if (number <= 77) firstWeekMocks++; else secondWeekMocks++;
            } else {
                assertTrue(canonicalAssignment.isMissingNode());
                assertTrue(ownerAssignment.isMissingNode());
            }
        }
        assertEquals(expectedMockDays, actualMockDays);
        assertEquals(2, firstWeekMocks);
        assertEquals(2, secondWeekMocks);
        assertTrue(questionBank.contains("<a id=\"q36\"></a>"));
        JsonNode canonicalDay81 = mapper.readTree(Files.readString(BASE.resolve("manifest.json"))).path("days").get(10);
        JsonNode ownerDay81 = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json"))).path("cards").get(10);
        for (JsonNode day81 : new JsonNode[]{canonicalDay81, ownerDay81}) {
            String scopeNote = day81.path("engineering").path("concept_course").path("scope_note").asText();
            assertFalse(scopeNote.contains("Sketch retained policy context"));
            assertTrue(scopeNote.contains("supplied") || scopeNote.contains("supplied mutually exclusive"));
        }
        assertFalse(ownerExport.contains("Sketch retained policy context"),
                "rendered owner export must not restore the obsolete learner capstone-sketch instruction");
        assertFalse(Files.readString(BASE.resolve("manifest.json")).contains("Sketch retained policy context"),
                "canonical rendered export must not restore the obsolete learner capstone-sketch instruction");
    }
}
