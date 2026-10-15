package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.analytics.api.CAP58Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP58AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-58/synthetic-v1.json");
    private final CAP58Seam seam = new CAP58Seam();
    private final CAP58Seam.TrustedAnalyticsContext allowed = new CAP58Seam.TrustedAnalyticsContext("owner-synthetic", true, Set.of(), Instant.parse("2026-10-14T12:00:00Z"));

    @Test void permitsAllowlistedPageViewAndUsesServerReceiveTime() throws Exception {
        var result = seam.validateAnalyticsBatch(bytes(event("page_view")), allowed);
        assertEquals(CAP58Seam.ResultStatus.VALID, result.status());
        assertEquals(1, result.events().size());
        assertEquals(allowed.serverReceivedAt(), result.events().getFirst().receivedAt());
    }

    @Test void rejectsPageTargetUnknownClickTargetBooleanDurationAndSensitiveExtras() throws Exception {
        for (String name : new String[]{"page_view_with_target", "unknown_target", "boolean_duration", "sensitive_extras"}) {
            var result = seam.validateAnalyticsBatch(bytes(event(name)), allowed);
            assertEquals(CAP58Seam.ResultStatus.INVALID, result.status(), name);
            assertTrue(result.events().isEmpty(), name);
        }
    }

    @Test void exactByteLimitPassesAndUtf8OverflowIsRejectedBeforeSchemaParsing() {
        var exact = seam.validateAnalyticsBatch(exactBody(), allowed);
        assertEquals(CAP58Seam.ResultStatus.VALID, exact.status());
        var overflow = seam.validateAnalyticsBatch("😀".repeat(2049).getBytes(java.nio.charset.StandardCharsets.UTF_8), allowed);
        assertEquals("BODY_TOO_LARGE", overflow.fieldCode());
    }

    @Test void disallowedCollectionReturnsDiscardWithoutPayload() throws Exception {
        var context = new CAP58Seam.TrustedAnalyticsContext("owner-synthetic", false, Set.of(), Instant.parse("2026-10-14T12:00:00Z"));
        var result = seam.validateAnalyticsBatch(bytes(event("page_view")), context);
        assertEquals(CAP58Seam.ResultStatus.DISCARDED_BY_POLICY, result.status());
        assertTrue(result.events().isEmpty());
    }

    private static JsonNode event(String name) throws Exception { return JSON.readTree(Files.readString(FIXTURE)).path("events").path(name); }
    private static byte[] bytes(JsonNode event) throws Exception {
        var root = JSON.createObjectNode(); var events = root.putArray("events"); events.add(event);
        return JSON.writeValueAsBytes(root);
    }
    private static byte[] exactBody() {
        try {
            byte[] base = bytes(event("page_view"));
            return (new String(base, java.nio.charset.StandardCharsets.UTF_8) + " ".repeat(8192 - base.length))
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
