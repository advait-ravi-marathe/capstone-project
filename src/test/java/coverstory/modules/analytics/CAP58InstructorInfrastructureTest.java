package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.ArrayList;

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

    @Test void suppliedIngressWitnessCovers202ReplayConflictAuthAndNoPartialBatch() throws Exception {
        var harness = new CAP58InstructorIngressHarness(Set.of("owner-A:journey-A"));
        byte[] page = batch(fixture().path("events").path("page_view"));
        var received = harness.ingest(page, true, true, "owner-A", null);
        assertEquals(new CAP58InstructorIngressHarness.Receipt(202, "RECEIVED", 1), received);
        assertEquals(new CAP58InstructorIngressHarness.Receipt(202, "DUPLICATE", 1), harness.ingest(page, true, true, "owner-A", null));
        JsonNode changed = JSON.readTree(page).path("events").get(0).deepCopy(); ((ObjectNode) changed).put("pageId", "compare");
        assertEquals("EVENT_ID_CONFLICT", harness.ingest(batch(changed), true, true, "owner-A", null).code());
        assertEquals(new CAP58InstructorIngressHarness.Receipt(413, "BODY_TOO_LARGE", 0), harness.ingest(new byte[8193], true, true, "owner-A", null));
        assertEquals(new CAP58InstructorIngressHarness.Receipt(202, "DISCARDED_BY_POLICY", 0), harness.ingest(page, false, true, "owner-A", null));
        assertEquals(401, harness.ingest(page, true, false, "owner-A", null).status());
        assertEquals(403, harness.ingest(page, true, true, "owner-B", "journey-A").status());
        assertEquals("INVALID_EVENT", harness.ingest(batch(fixture().path("events").path("sensitive_extras")), true, true, "owner-A", null).code());
        ObjectNode root = JSON.createObjectNode();
        var events = root.putArray("events");
        for (int i = 0; i < 10; i++) { ObjectNode e = (ObjectNode) fixture().path("events").path("page_view").deepCopy(); e.put("eventId", String.format("58000000-0000-4000-8000-%012d", i+10)); events.add(e); }
        assertEquals(202, harness.ingest(JSON.writeValueAsBytes(root), true, true, "owner-A", null).status());
        events.add(events.get(0).deepCopy());
        assertEquals(400, harness.ingest(JSON.writeValueAsBytes(root), true, true, "owner-A", null).status());
    }

    @Test void boundedBrowserTelemetryQueueDropsAfterThreeAttemptsWithoutControllingEssentialWork() {
        var queue = new CAP58InstructorIngressHarness.BrowserQueue();
        for (int i = 0; i < 20; i++) assertTrue(queue.enqueue(new byte[]{(byte) i}));
        assertFalse(queue.enqueue(new byte[]{21}));
        final boolean[] checkoutContinues = {false};
        var result = queue.flush(attempt -> false);
        checkoutContinues[0] = true;
        assertEquals(0, result.sent()); assertEquals(3, result.attempts());
        assertEquals(1, result.dropped()); assertEquals(19, result.queued());
        assertTrue(checkoutContinues[0], "telemetry loss cannot block checkout or resume");
    }

    private static byte[] batch(JsonNode event) throws Exception {
        var root = JSON.createObjectNode(); root.putArray("events").add(event); return JSON.writeValueAsBytes(root);
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }

    private static boolean referenceAccepts(JsonNode e) {
        if (!e.isObject()) return false;
        for (String name : e.propertyNames()) if (!FIELDS.contains(name)) return false;
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
