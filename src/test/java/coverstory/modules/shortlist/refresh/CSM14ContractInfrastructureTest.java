package coverstory.modules.shortlist.refresh;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/** Instructor checks for the supplied synthetic vectors and deliberately incomplete learner seams. */
class CSM14ContractInfrastructureTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    private JsonNode fixture(int day) throws Exception {
        return JSON.readTree(Files.readString(Path.of("fixtures/coverstory/CSM14-%02d/synthetic-inputs.json".formatted(day))));
    }
    private JsonNode scenario(int day) throws Exception { return fixture(day).path("scenario"); }

    @Test void allFourteenFixturesAreSyntheticAndHaveDistinctSourceHashes() throws Exception {
        for(int day=1;day<=14;day++) {
            JsonNode f=fixture(day);
            assertEquals("CSM14-%02d-synthetic-v1".formatted(day),f.path("fixture_id").asText());
            assertEquals("SYNTHETIC_ONLY",f.path("classification").asText());
            assertEquals(64,f.path("description_sha256").asText().length());
            assertFalse(Files.readString(Path.of("fixtures/coverstory/CSM14-%02d/synthetic-inputs.json".formatted(day))).matches("(?is).*\\b(ssn|email|phone|health_answer|access_token)\\b.*"));
        }
    }
    @Test void cap632FixtureHasThreeStableSlotsAndUnavailableIsNotDeletion() throws Exception {
        JsonNode s=scenario(1); assertEquals(3,s.path("checkpoint").path("slots").size());
        assertEquals("UNAVAILABLE",s.path("unavailableLookup").path("status").asText());
        assertEquals("s3",s.path("unavailableLookup").path("slotId").asText());
        assertTrue(s.path("checkpoint").path("slots").get(0).path("source").has("sourceSha256"));
    }
    @Test void cap633FixtureSeparatesDivergenceRemovalAndUnavailable() throws Exception {
        JsonNode s=scenario(2); assertEquals("CONFLICT",s.path("truthTable").path("DIVERGENT").asText());
        assertEquals("REMOVE",s.path("local").path("s3").path("operation").asText());
        assertTrue(s.path("unavailable").asText().contains("separate"));
    }
    @Test void cap634FixtureNamesAtomicFailureBoundary() throws Exception {
        JsonNode s=scenario(3); assertEquals("after revision staging, before required audit/journey effects",s.path("faultPoint").asText());
        assertEquals(4,s.path("expectedRollback").size());
    }
    @Test void cap635FixtureUsesControlledTwoTabRaceAndReplay() throws Exception {
        JsonNode s=scenario(4); assertTrue(s.path("barrier").asText().contains("both transactions"));
        assertEquals(2,s.path("branches").size()); assertEquals(3,s.path("replayCases").size());
    }
    @Test void cap636FixtureBoundsReadsAndIncludesOwnerAndSnapshotAdversaries() throws Exception {
        JsonNode s=scenario(5); assertEquals(3,s.path("maxSelectedProducts").asInt());
        assertEquals(9,s.path("maxExactVersionReads").asInt()); assertTrue(s.path("cases").toString().contains("foreign owner"));
    }
    @Test void cap637FixtureHasCurrentAndStaleGenerationAndFaultCase() throws Exception {
        JsonNode s=scenario(6); assertEquals(20,s.path("preview").path("catalogGeneration").asInt());
        assertEquals(21,s.path("staleGeneration").asInt()); assertTrue(s.path("expected").toString().contains("rolls back"));
    }
    @Test void cap638FixtureRequiresFingerprintBoundAccessibleConflictChoices() throws Exception {
        JsonNode s=scenario(7); assertEquals(3,s.path("slots").size());
        assertEquals("CONFLICT",s.path("slots").get(1).path("state").asText());
        assertTrue(s.path("requiredUi").toString().contains("keyboard choice"));
    }
    @Test void cap639FixtureSeparatesCommitDeliveryAndReload() throws Exception {
        JsonNode s=scenario(8); assertEquals("response suppressed after commit",s.path("transport").asText());
        assertTrue(s.path("reload").toString().contains("unsaved memory choices not recovered"));
    }
    @Test void cap640FixtureModelsBothAuthorizationOrdersAndOwnerSwitch() throws Exception {
        JsonNode s=scenario(9); assertEquals(4,s.path("previewAuthGeneration").asInt());
        assertEquals(5,s.path("revokedGeneration").asInt());
        assertEquals("SYNTHETIC_OWNER_B",s.path("currentSessionOwner").asText());
    }
    @Test void cap641FixtureNeverGuessesHistoricalProvenance() throws Exception {
        JsonNode s=scenario(10); assertEquals("UNKNOWN/UNRECORDED",s.path("oldRows").get(0).path("provenance").asText());
        assertEquals(99,s.path("unsupportedRequiredMajor").asInt());
    }
    @Test void cap642FixtureHasMatchedBoundedSyntheticDatasets() throws Exception {
        JsonNode s=scenario(11); assertEquals(20,s.path("pageSize").asInt());
        assertEquals(1000,s.path("owners").path("A").asInt());
        assertTrue(s.path("measurement").path("syntheticOnly").asBoolean());
    }
    @Test void cap643FixtureSeparatesSeededAndLearnerAuthoredTraces() throws Exception {
        JsonNode s=scenario(12); assertEquals(12,s.path("seededTraceCount").asInt());
        assertEquals(4,s.path("learnerAuthoredFreshTraceCount").asInt());
        assertEquals(6,s.path("maxCommandsPerTrace").asInt());
    }
    @Test void cap644FixtureSeparatesLocalOrderFromReverseMapOrder() throws Exception {
        JsonNode s=scenario(13); assertEquals(s.path("localOrder"),s.path("expectedOrder"));
        assertNotEquals(s.path("localOrder"),s.path("upstreamMapOrder"));
        assertEquals("s2",s.path("expectedConflicts").get(0).asText());
    }
    @Test void cap645FixtureHasSixEventsAndAStaleOverwriteDefect() throws Exception {
        JsonNode s=scenario(14); assertEquals(6,s.path("sixEventSchedule").size());
        assertTrue(s.path("defectiveVariant").asText().contains("overwrites"));
    }
}
