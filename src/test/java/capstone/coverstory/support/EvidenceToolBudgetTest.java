package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceToolBudget;
import capstone.coverstory.support.internal.EvidenceToolBudget.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class EvidenceToolBudgetTest {
    private static Proposal p(String query) { return new Proposal("application-status", "owner-A", "journey-A", query); }
    @Test void distinctProposalListIsCappedAtThreeAndReportsStepLimit() {
        var result = new EvidenceToolBudget().investigate(List.of(p("a"),p("b"),p("c"),p("d")), x -> new ReadResult("r", 1, Dispatch.NOT_DISPATCHED), 2500);
        assertEquals(3, result.trace().size()); assertEquals(Stop.STEP_LIMIT, result.stop());
    }
    @Test void repeatedCanonicalSignatureDoesNotReadTwice() {
        int[] reads = {0};
        var result = new EvidenceToolBudget().investigate(List.of(p("status"),p(" STATUS ")), x -> { reads[0]++; return new ReadResult("r", 1, Dispatch.NOT_DISPATCHED); }, 2500);
        assertEquals(1, reads[0]); assertEquals(Stop.REPEATED_CALL, result.stop());
    }
    @Test void deadlineOrUnknownDispatchRequiresReconciliationAndAbstains() {
        var result = new EvidenceToolBudget().investigate(List.of(p("dispatch"),p("payment")), x -> new ReadResult("synthetic", 2500, Dispatch.UNKNOWN), 2500);
        assertEquals(1, result.trace().size()); assertTrue(List.of(Stop.DEADLINE, Stop.RECONCILIATION_REQUIRED).contains(result.stop()));
        assertEquals("INSUFFICIENT_EVIDENCE", result.answer());
    }
}
