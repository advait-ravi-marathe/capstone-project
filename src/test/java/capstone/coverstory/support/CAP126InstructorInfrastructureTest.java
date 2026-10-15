package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceToolBudget;
import capstone.coverstory.support.internal.EvidenceToolBudget.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Offline contract oracle: deterministic fake read port, no database/network/model/writes. */
class CAP126InstructorInfrastructureTest {
    private static Proposal p(String query) { return new Proposal("application-status", "owner-A", "journey-A", query); }
    private static ReadOnlyEvidencePort quick(Dispatch dispatch) {
        return ignored -> new ReadResult("synthetic read", 10, dispatch);
    }

    @Test void threeDistinctCallsExecuteAndFourthIsStepLimited() {
        int[] reads = {0};
        var result = CAP126ReferenceOracle.investigate(List.of(p("a"),p("b"),p("c"),p("d")), x -> { reads[0]++; return new ReadResult("r", 1, Dispatch.NOT_DISPATCHED); }, 2500);
        assertEquals(3, reads[0]); assertEquals(3, result.trace().size()); assertEquals(Stop.STEP_LIMIT, result.stop());
    }
    @Test void canonicalDuplicateStopsBeforeSecondRead() {
        int[] reads = {0};
        var result = CAP126ReferenceOracle.investigate(List.of(p("status"),p(" STATUS ")), x -> { reads[0]++; return new ReadResult("r", 1, Dispatch.NOT_DISPATCHED); }, 2500);
        assertEquals(1, reads[0]); assertEquals(Stop.REPEATED_CALL, result.stop());
    }
    @Test void deadlineAndUnknownDispatchBothStopWithoutInventingOutcome() {
        var deadline = CAP126ReferenceOracle.investigate(List.of(p("status"),p("payment")), x -> new ReadResult("synthetic", 2500, Dispatch.NOT_DISPATCHED), 2500);
        assertEquals(1, deadline.trace().size()); assertEquals(Stop.DEADLINE, deadline.stop()); assertEquals("INSUFFICIENT_EVIDENCE", deadline.answer());
        var unknown = CAP126ReferenceOracle.investigate(List.of(p("dispatch"),p("again")), quick(Dispatch.UNKNOWN), 2500);
        assertEquals(1, unknown.trace().size()); assertEquals(Stop.RECONCILIATION_REQUIRED, unknown.stop());
        assertEquals("INSUFFICIENT_EVIDENCE", unknown.answer());
    }
    @Test void oracleHasNoMutationOperation() {
        assertTrue(java.util.Arrays.stream(EvidenceToolBudget.class.getDeclaredMethods())
                .noneMatch(m -> m.getName().matches("(?i).*(cancel|refund|dispatch|write|mutate).*")));
    }
}
