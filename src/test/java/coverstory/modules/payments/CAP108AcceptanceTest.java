package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP108Seam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance contract; TODO failures are expected until the assignment is implemented. */
@Tag("learnerAcceptance")
class CAP108AcceptanceTest {
    @Test void backfillAdvancesOnlyItsOwnCursorAndDoesNoMoneyAction() {
        var result = new CAP108Seam().recompute(40, 120, ids(41,50), null, List.of());
        assertEquals(50, result.backfillCursor()); assertEquals(10, result.staged());
        assertEquals(120, result.liveCursor()); assertEquals(0, result.moneyActions());
    }
    @Test void twoLiveAndTwoBackfillJobsRespectReservedLaneCapacity() {
        var jobs = List.of(new CAP108Seam.Job("l1",CAP108Seam.Lane.LIVE),new CAP108Seam.Job("b1",CAP108Seam.Lane.BACKFILL),new CAP108Seam.Job("l2",CAP108Seam.Lane.LIVE),new CAP108Seam.Job("b2",CAP108Seam.Lane.BACKFILL));
        var plan = new CAP108Seam().plan(jobs);
        assertEquals(List.of("l1","b1","l2"), plan.running().stream().map(CAP108Seam.Job::id).toList());
        assertEquals(List.of("b2"), plan.queued().stream().map(CAP108Seam.Job::id).toList());
    }
    @Test void failureDoesNotPublishPartialDiagnosticsAndNamesFailedRun() {
        var old = List.of(new CAP108Seam.Adjustment("capture:x","CAPTURE","CONFIRMED","r1",0));
        var result = new CAP108Seam().recompute(40,120,ids(41,50),45L,old);
        assertEquals(old,result.diagnostics()); assertEquals(120,result.liveCursor()); assertEquals("cap108-run-45",result.failedRunId());
    }
    @Test void pendingRefundRevisionChangeKeepsAllocationReserved() {
        var pending = new CAP108Seam.Adjustment("refund:pay-A:revision-7","REFUND","PENDING","r8",1200);
        var result = new CAP108Seam().recompute(40,120,List.of(41L),null,List.of(pending));
        assertEquals("PENDING", result.diagnostics().get(0).status()); assertEquals(1200,result.diagnostics().get(0).reservedMinor()); assertEquals(0,result.moneyActions());
    }
    @Test void liveAndHistoricalCopiesCoalesceByEffectIdentityWithoutMoneyAction() {
        var effects = List.of(new CAP108Seam.Adjustment("capture:pay-A:attempt-1","CAPTURE","CONFIRMED","r1",0),new CAP108Seam.Adjustment("capture:pay-A:attempt-1","CAPTURE","CONFIRMED","r1",0));
        var result = new CAP108Seam().recompute(40,120,List.of(41L),null,effects);
        assertEquals(1,result.diagnostics().stream().filter(x -> x.effectIdentity().equals("capture:pay-A:attempt-1")).count()); assertEquals(0,result.moneyActions());
    }
    private static List<Long> ids(long start,long end){return java.util.stream.LongStream.rangeClosed(start,end).boxed().toList();}
}
