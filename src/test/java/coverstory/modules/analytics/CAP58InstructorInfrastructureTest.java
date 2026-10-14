package coverstory.modules.analytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent local fixture oracle; it does not call the learner seam. */
class CAP58InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-58/synthetic-v1.json");
    private static final Set<String> PAGES = Set.of("health_discovery", "compare", "quote_review", "home", "checkout", "onboarding", "final_review");
    private static final Set<String> TARGETS = Set.of("compare_plan", "save_draft");
    private static final Set<String> FIELDS = Set.of("eventId", "eventType", "schemaVersion", "pseudonymousSessionId", "pageId", "targetId", "optionalDurationMs", "clientTime", "stepCode");

    @Test void fixtureDeclaresSyntheticScopeAndExactLimits() throws Exception {
        JsonNode f = fixture();
        assertEquals("SYNTHETIC_ALLOWLISTED_ANALYTICS_CONTRACT", f.path("classification").asText());
        assertEquals(8192, f.path("limits").path("max_utf8_bytes").asInt());
        assertEquals(10, f.path("limits").path("max_events").asInt());
        assertEquals(300000, f.path("limits").path("max_duration_ms").asInt());
        assertFalse(f.path("client_time_authoritative").asBoolean());
        assertFalse(f.path("persisted").asBoolean());
        assertFalse(f.path("journey_state_changes").asBoolean());
    }

    @Test void independentAllowlistAcceptsPermittedSamplesAndRejectsUnsafeFieldShapes() throws Exception {
        JsonNode e = fixture().path("events");
        assertTrue(referenceAccepts(e.path("page_view")));
        assertTrue(referenceAccepts(e.path("control_click")));
        assertFalse(referenceAccepts(e.path("page_view_with_target")));
        assertFalse(referenceAccepts(e.path("unknown_target")));
        assertFalse(referenceAccepts(e.path("boolean_duration")));
        assertFalse(referenceAccepts(e.path("sensitive_extras")));
    }

    @Test void durationBoundsAreIntegerOnlyAndUnknownFieldsAreRejected() throws Exception {
        JsonNode valid = fixture().path("events").path("control_click");
        assertTrue(referenceAccepts(((ObjectNode) valid.deepCopy()).put("optionalDurationMs", 0)));
        assertTrue(referenceAccepts(((ObjectNode) valid.deepCopy()).put("optionalDurationMs", 300000)));
        assertFalse(referenceAccepts(((ObjectNode) valid.deepCopy()).put("optionalDurationMs", 300001)));
        assertFalse(referenceAccepts(((ObjectNode) valid.deepCopy()).put("optionalDurationMs", true)));
        assertFalse(referenceAccepts(((ObjectNode) valid.deepCopy()).put("arbitrary", "synthetic-marker")));
    }

    @Test void bodyByteCapCountsUtf8AndNotJavaCharacters() {
        byte[] exact = ("{" + " ".repeat(8190) + "}").getBytes(StandardCharsets.UTF_8);
        byte[] over = ("{" + " ".repeat(8191) + "}").getBytes(StandardCharsets.UTF_8);
        byte[] multibyte = "😀".repeat(2049).getBytes(StandardCharsets.UTF_8);
        assertEquals(8192, exact.length);
        assertEquals(8193, over.length);
        assertEquals(8196, multibyte.length);
        assertTrue(exact.length <= 8192);
        assertTrue(over.length > 8192);
        assertTrue(multibyte.length > 8192);
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }

    private static boolean referenceAccepts(JsonNode e) {
        if (!e.isObject()) return false;
        var names = e.fieldNames();
        while (names.hasNext()) if (!FIELDS.contains(names.next())) return false;
        if (!e.path("eventId").isTextual() || !e.path("eventId").asText().matches("(?i)[0-9a-f-]{36}")) return false;
        if (!e.path("pseudonymousSessionId").isTextual() || !e.path("pseudonymousSessionId").asText().matches("(?i)[0-9a-f-]{36}")) return false;
        if (e.path("schemaVersion").asInt(-1) != 1 || !e.path("pageId").isTextual() || !PAGES.contains(e.path("pageId").asText())) return false;
        String type = e.path("eventType").asText();
        boolean targetPresent = e.has("targetId");
        if (type.equals("PAGE_VIEW") && targetPresent) return false;
        if (type.equals("CONTROL_CLICK") && (!targetPresent || !e.path("targetId").isTextual() || !TARGETS.contains(e.path("targetId").asText()))) return false;
        if (!type.equals("PAGE_VIEW") && !type.equals("CONTROL_CLICK")) return false;
        if (e.has("optionalDurationMs")) {
            JsonNode d = e.get("optionalDurationMs");
            if (!d.isIntegralNumber() || d.asInt() < 0 || d.asInt() > 300000) return false;
        }
        return true;
    }
}
