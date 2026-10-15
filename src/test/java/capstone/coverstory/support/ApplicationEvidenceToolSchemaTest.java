package capstone.coverstory.support;

import capstone.coverstory.support.internal.ApplicationEvidenceToolSchema;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ApplicationEvidenceToolSchemaTest {
    @Test void validReadUsesExternalPrincipalAndReturnsOnlyEvidence() {
        var result = ApplicationEvidenceToolSchema.invoke(new ApplicationEvidenceToolSchema.Principal("session-7", "owner-A"),
                Map.of("applicationId", "app-A1", "limit", 2), (owner, app, limit) -> List.of());
        assertEquals("OK", result.status());
    }
    @Test void mutationSqlAndOwnerArgumentsRejectBeforeStore() {
        AtomicInteger calls = new AtomicInteger();
        var api = (ApplicationEvidenceToolSchema.ApplicationReadApi) (o, a, l) -> { calls.incrementAndGet(); return List.of(); };
        for (var args : List.<Map<String, ?>>of(Map.of("applicationId", "app-A1", "limit", 2, "cancel", true),
                Map.of("applicationId", "app-A1", "limit", 2, "submit", true),
                Map.of("applicationId", "app-A1", "limit", 2, "sql", "select 1"),
                Map.of("applicationId", "app-A1", "limit", 2, "ownerId", "B")))
            assertEquals("SCHEMA_REJECTED", ApplicationEvidenceToolSchema.invoke(new ApplicationEvidenceToolSchema.Principal("s", "owner-A"), args, api).code());
        assertEquals(0, calls.get());
    }
    @Test void strictLimitRejectsOutOfRangeAndCoercion() {
        var api = (ApplicationEvidenceToolSchema.ApplicationReadApi) (o, a, l) -> List.of();
        for (Object n : List.of(0, 51, "two"))
            assertEquals("SCHEMA_REJECTED", ApplicationEvidenceToolSchema.invoke(new ApplicationEvidenceToolSchema.Principal("s", "owner-A"), Map.of("applicationId", "app-A1", "limit", n), api).code());
    }
}
