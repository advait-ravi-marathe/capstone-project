package capstone.coverstory.application.internal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP175AcceptanceTest {
    private static final UUID OWNER=UUID.fromString("17500000-0000-4000-8000-000000000001");
    private static final Instant START=Instant.parse("2026-12-29T10:00:00Z");
    private List<SubmissionTraceLinker.Span> spans() { return List.of(
            new SubmissionTraceLinker.Span("submit","trace-a",OWNER,"EXPLICIT_SUBMIT",START,START.plusSeconds(1),"SUBMISSION_PENDING"),
            new SubmissionTraceLinker.Span("dispatch","trace-a",OWNER,"SIMULATED_DISPATCH",START.plusSeconds(1),START.plusSeconds(3),"SUBMISSION_PENDING"),
            new SubmissionTraceLinker.Span("support","trace-b",OWNER,"READ_ONLY_INVESTIGATION",START.plusSeconds(3600),START.plusSeconds(3601),"SUBMISSION_PENDING")); }
    @Test void dispatchAndLaterInvestigationUsePersistedLinksWithoutChangingTimes() {
        var spans=spans();
        var result=SubmissionTraceLinker.correlate(OWNER,"submit","dispatch","support",spans);
        assertEquals(2,result.links().size()); assertEquals("SUBMISSION_PENDING",spans.get(0).applicationStatus());
        assertEquals(START.plusSeconds(1),spans.get(1).startedAt());
    }
    @Test void unknownReferenceStaysUnresolvedAndForeignOwnerIsDenied() {
        var spans=spans();
        var unknown=SubmissionTraceLinker.correlate(OWNER,"submit","absent","support",spans);
        assertEquals("absent",unknown.unresolvedReference());
        var foreign=SubmissionTraceLinker.correlate(UUID.randomUUID(),"submit","dispatch","support",spans);
        assertFalse(foreign.authorized()); assertTrue(foreign.links().isEmpty());
    }
    @Test void todoGuardKeepsTheLinkerUnimplemented() {
        assertThrows(UnsupportedOperationException.class,()->SubmissionTraceLinker.correlate(OWNER,"submit","dispatch","support",spans()));
    }
}
