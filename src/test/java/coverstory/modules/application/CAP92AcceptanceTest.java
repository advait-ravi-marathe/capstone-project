package coverstory.modules.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.application.api.CAP92Seam;
import coverstory.modules.application.api.CAP92Seam.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP92AcceptanceTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-92/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void rawBodyBoundaryIsInclusiveAndOlderSourceIsOnlyProposed() throws Exception {
        JsonNode fixture = fixture();
        UUID owner = owner(fixture, "A");
        RetentionPlan plan = new CAP92Seam().classifyRetention(owner, fixedClock(fixture), records(fixture));
        assertEquals(Decision.RETAIN, row(plan, "raw-at-boundary").decision());
        assertEquals(Decision.CANDIDATE_DELETE, row(plan, "raw-one-second-old").decision());
        assertEquals(1, plan.proposedDeletes());
        assertEquals(0, plan.deleteCalls());
    }

    @Test void auditReferenceRetainsOnlyNonContentMetadata() throws Exception {
        JsonNode fixture = fixture();
        RetentionPlan plan = new CAP92Seam().classifyRetention(owner(fixture, "A"), fixedClock(fixture), records(fixture));
        ReportRow audit = row(plan, "audit-31d");
        assertEquals(Decision.RETAIN, audit.decision());
        assertFalse(audit.bodyPresent());
        assertEquals("ref-retained-31d", audit.referenceId());
        assertFalse(JSON.writeValueAsString(plan).contains("SYNTHETIC_PRIVATE_BODY"));
    }

    @Test void ownerIsFilteredBeforeClassificationAndNoDeletionCanRun() throws Exception {
        JsonNode fixture = fixture();
        UUID owner = owner(fixture, "A");
        AtomicInteger deleteCalls = new AtomicInteger();
        RetentionPlan plan = new CAP92Seam().classifyRetention(owner, fixedClock(fixture), records(fixture));
        deleteCalls.addAndGet(plan.deleteCalls());
        assertTrue(plan.rows().stream().noneMatch(row -> row.sourceId().equals("same-id-other-owner")));
        assertEquals(0, deleteCalls.get());
    }

    @Test void auditBoundaryIsInclusiveAndOlderAuditIsOnlyCandidate() throws Exception {
        JsonNode fixture = fixture();
        RetentionPlan plan = new CAP92Seam().classifyRetention(owner(fixture, "A"), fixedClock(fixture), records(fixture));
        assertEquals(Decision.RETAIN, row(plan, "audit-90d").decision());
        assertEquals(Decision.CANDIDATE_DELETE, row(plan, "audit-90d-plus-1s").decision());
        assertEquals(0, plan.deleteCalls());
    }

    @Test void futureAndNullTimesAreInvalidRatherThanNegativeAgeRetention() throws Exception {
        JsonNode fixture = fixture();
        RetentionPlan plan = new CAP92Seam().classifyRetention(owner(fixture, "A"), fixedClock(fixture), records(fixture));
        assertEquals(Decision.INVALID_TIME, row(plan, "future-time").decision());
        assertEquals(Decision.INVALID_TIME, row(plan, "null-time").decision());
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static UUID owner(JsonNode fixture, String key) { return UUID.fromString(fixture.path("owners").path(key).asText()); }
    private static Clock fixedClock(JsonNode fixture) { return Clock.fixed(Instant.parse(fixture.path("fixed_clock").asText()), ZoneOffset.UTC); }
    private static List<SourceRecord> records(JsonNode fixture) {
        List<SourceRecord> rows = new ArrayList<>();
        for (JsonNode row : fixture.path("records")) {
            String date = row.path("created_at").isNull() ? null : row.path("created_at").asText();
            rows.add(new SourceRecord(owner(fixture, row.path("owner").asText()), row.path("id").asText(),
                    SourceKind.valueOf(row.path("kind").asText()), date == null ? null : Instant.parse(date),
                    row.path("reference_id").isNull() ? null : row.path("reference_id").asText()));
        }
        return rows;
    }
    private static ReportRow row(RetentionPlan plan, String id) {
        return plan.rows().stream().filter(row -> row.sourceId().equals(id)).findFirst().orElseThrow();
    }
}
