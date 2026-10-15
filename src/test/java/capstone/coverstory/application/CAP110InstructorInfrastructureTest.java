package capstone.coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CAP110InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("docs/evidence/coverstory/CAP-110/fixtures.json");

    @Test void syntheticFixtureIsLocalAndDefinesNoExternalEffects() throws Exception {
        JsonNode fixture = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_ONLY_NO_NETWORK", fixture.path("classification").asText());
        assertFalse(fixture.path("network_enabled").asBoolean());
        assertFalse(fixture.path("dispatch_enabled").asBoolean());
        assertFalse(fixture.path("real_provider_credentials").asBoolean());
        assertTrue(fixture.path("configuration_cases").size() >= 5);
        assertTrue(fixture.path("review_cases").size() >= 6);
    }

    @Test void validConfigurationProducesBoundedSecretFreeManifest() {
        var result = CAP110InstructorOracle.validateConfig(Map.of(
                "provider.mode", "mock", "timeoutMillis", "1500", "inputBudget", "2000",
                "outputBudget", "256", "provider.apiKey", "SECRET_SENTINEL"));
        assertTrue(result.valid());
        assertEquals("VALID", result.code());
        assertEquals(true, result.manifest().get("secretConfigured"));
        assertFalse(result.manifest().toString().contains("SECRET_SENTINEL"));
        assertEquals(false, result.manifest().get("networkCallsAllowed"));
    }

    @Test void unsafeLimitsLiveModeAndUnknownBypassKeysAreRejected() {
        for (Map<String, String> unsafe : new Map[]{
                Map.of("provider.mode", "mock", "timeoutMillis", "0", "inputBudget", "2", "outputBudget", "1"),
                Map.of("provider.mode", "live", "timeoutMillis", "1", "inputBudget", "2", "outputBudget", "1"),
                Map.of("provider.mode", "mock", "timeoutMillis", "1", "inputBudget", "2", "outputBudget", "1", "autoSubmit", "true"),
                Map.of("provider.mode", "mock", "timeoutMillis", "1", "inputBudget", "2", "outputBudget", "-1")})
            assertEquals("STARTUP_REJECTED", CAP110InstructorOracle.validateConfig(unsafe).code());
    }

    @Test void eachMissingReviewFactReturnsAnExplicitBlockerWithoutEffects() {
        var allTrue = CAP110InstructorOracle.evaluate(true, true, true, true, true, true, true, true, false);
        assertTrue(allTrue.ready());
        assertFalse(allTrue.submissionPending());
        assertFalse(allTrue.dispatchCreated());
        assertFalse(CAP110InstructorOracle.evaluate(true, false, true, true, true, true, true, true, true).ready());
        assertFalse(CAP110InstructorOracle.evaluate(true, true, true, false, true, true, true, true, true).ready());
        assertFalse(CAP110InstructorOracle.evaluate(true, true, true, true, false, true, true, true, true).ready());
        assertFalse(CAP110InstructorOracle.evaluate(true, true, true, true, true, false, true, true, true).ready());
        assertFalse(CAP110InstructorOracle.evaluate(true, true, true, true, true, true, false, true, true).ready());
        assertFalse(CAP110InstructorOracle.evaluate(true, true, true, true, true, true, true, false, true).ready());
    }

    @Test void explicitActionIsNecessaryButOracleNeverCreatesPendingOrDispatchEffects() {
        var draftReady = CAP110InstructorOracle.evaluate(true, true, true, true, true, true, true, true, false);
        var explicit = CAP110InstructorOracle.evaluate(true, true, true, true, true, true, true, true, true);
        assertTrue(draftReady.ready() && explicit.ready());
        assertFalse(draftReady.submissionPending() || explicit.submissionPending());
        assertFalse(draftReady.dispatchCreated() || explicit.dispatchCreated());
    }
}
