package capstone.coverstory.support;

import capstone.coverstory.support.internal.ApplicationEvidenceToolSchema;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CAP124InstructorInfrastructureTest {
    @Test void fixtureIsFrozenAndSynthetic() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-124.json"));
        assertEquals("df10dfc59b0b72eb6695612401d1133f1e2a7070e75f6ac4a71f915ae739b1f5", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        var f = new ObjectMapper().readTree(bytes);
        assertFalse(f.path("network_enabled").asBoolean());
        assertEquals("synthetic-owner-A", f.path("principal").path("owner_id").asText());
        assertEquals(2, f.path("expected_rows").size());
    }
    @Test void instructorSchemaAcceptsOnlyReadShapeAndKeepsIdentityOutsideArguments() {
        AtomicInteger reads = new AtomicInteger();
        var api = (ApplicationEvidenceToolSchema.ApplicationReadApi) (owner, app, limit) -> {
            reads.incrementAndGet(); assertEquals("synthetic-owner-A", owner); assertEquals("app-A1", app); assertEquals(2, limit);
            return List.of(new ApplicationEvidenceToolSchema.EvidenceRow("e1", "APPLICATION_CREATED", "2026-01-01T00:00:00Z", "Application started"));
        };
        var accepted = CAP124ReferenceOracle.invoke("synthetic-owner-A", Map.of("applicationId", "app-A1", "limit", 2), api);
        assertEquals("OK", accepted.status()); assertEquals(1, reads.get());
        for (var args : List.<Map<String, ?>>of(
                Map.of("applicationId", "app-A1", "limit", 2, "cancel", true),
                Map.of("applicationId", "app-A1", "limit", 2, "submit", true),
                Map.of("applicationId", "app-A1", "limit", 2, "sql", "DELETE FROM applications"),
                Map.of("applicationId", "app-A1", "limit", 2, "ownerId", "B"))) {
            assertEquals("SCHEMA_REJECTED", CAP124ReferenceOracle.invoke("synthetic-owner-A", args, api).code());
        }
        assertEquals(1, reads.get(), "schema failures must happen before owning API call");
    }
    @Test void integerBoundaryAndAuthenticatedContextAreStrict() {
        AtomicInteger reads = new AtomicInteger();
        var api = (ApplicationEvidenceToolSchema.ApplicationReadApi) (owner, app, limit) -> { reads.incrementAndGet(); return List.of(); };
        for (Object n : List.of(0, 51, "two", 2L)) {
            assertEquals("SCHEMA_REJECTED", CAP124ReferenceOracle.invoke("owner-A", Map.of("applicationId", "app-A1", "limit", n), api).code());
        }
        assertEquals("UNAUTHENTICATED", CAP124ReferenceOracle.invoke(null, Map.of("applicationId", "app-A1", "limit", 2), api).code());
        assertEquals(0, reads.get());
    }
}
