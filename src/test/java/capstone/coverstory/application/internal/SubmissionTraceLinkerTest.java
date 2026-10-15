package capstone.coverstory.application.internal;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SubmissionTraceLinkerTest {
    private static final UUID OWNER=UUID.fromString("17500000-0000-4000-8000-000000000001");
    private static final Instant T=Instant.parse("2026-12-29T10:00:00Z");
    private static List<SubmissionTraceLinker.Span> spans(){return List.of(
            new SubmissionTraceLinker.Span("submit","trace-a",OWNER,"EXPLICIT_SUBMIT",T,T.plusSeconds(1),"SUBMISSION_PENDING"),
            new SubmissionTraceLinker.Span("dispatch","trace-a",OWNER,"SIMULATED_DISPATCH",T.plusSeconds(1),T.plusSeconds(3),"SUBMISSION_PENDING"),
            new SubmissionTraceLinker.Span("support","trace-b",OWNER,"READ_ONLY_INVESTIGATION",T.plusSeconds(3600),T.plusSeconds(3601),"SUBMISSION_PENDING"));}
    @Test void laterInvestigationLinksToCompletedDispatchWithoutBecomingItsChild(){
        var rows=spans(); var result=CAP175InstructorOracle.correlate(OWNER,"submit","dispatch","support",rows);
        assertTrue(result.authorized()); assertEquals(2,result.links().size());
        assertEquals("SUBMISSION_TO_DISPATCH",result.links().get(0).relation());
        assertEquals("LATER_SUPPORT_INVESTIGATION",result.links().get(1).relation());
        assertNotEquals("trace-a",rows.get(2).traceId()); assertEquals("SUBMISSION_PENDING",rows.get(0).applicationStatus());
        assertEquals(T.plusSeconds(3),rows.get(1).endedAt());
    }
    @Test void unknownSpanIsUnresolvedAndNeverFabricated(){var result=CAP175InstructorOracle.correlate(OWNER,"submit","missing","support",spans());assertEquals("missing",result.unresolved());assertTrue(result.links().isEmpty());}
    @Test void ownerMismatchDeniesTheWholeRead(){var result=CAP175InstructorOracle.correlate(UUID.randomUUID(),"submit","dispatch","support",spans());assertFalse(result.authorized());assertTrue(result.links().isEmpty());}
    @Test void fixtureContainsOnlySafeSyntheticTraceMetadata() throws Exception {
        var p=java.nio.file.Path.of("docs/coverstory/fixtures/cap-175.json");
        var n=new tools.jackson.databind.ObjectMapper().readTree(java.nio.file.Files.readString(p));
        assertEquals("SYNTHETIC_ONLY_NO_NETWORK",n.path("classification").asText());
        assertFalse(n.toString().contains("SECRET_SENTINEL"));assertEquals(3,n.path("spans").size());
    }
}
