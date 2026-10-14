package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP108Seam;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Test-only oracle for the assignment contract; it does not call the learner seam. */
class CAP108InstructorInfrastructureTest {
    private static CAP108Seam.Plan oraclePlan(List<CAP108Seam.Job> jobs) {
        var live = new ArrayList<CAP108Seam.Job>(); var backfill = new ArrayList<CAP108Seam.Job>();
        for (var j : jobs) (j.lane()==CAP108Seam.Lane.LIVE ? live : backfill).add(j);
        var running = new ArrayList<CAP108Seam.Job>(); var queued = new ArrayList<CAP108Seam.Job>();
        int i=0;
        while(i<live.size() || i<backfill.size()) {
            if(i<live.size()) running.add(live.get(i));
            if(i<backfill.size()) { if(i==0) running.add(backfill.get(i)); else queued.add(backfill.get(i)); }
            i++;
        }
        return new CAP108Seam.Plan(running,queued);
    }
    private static CAP108Seam.BackfillResult oracle(long backfill, long live, List<Long> ids, Long failAt, List<CAP108Seam.Adjustment> published) {
        if(failAt!=null && ids.contains(failAt)) return new CAP108Seam.BackfillResult(backfill,0,live,0,"cap108-run-"+failAt,published);
        long cursor=backfill;
        for(long id:ids) { if(id!=cursor+1) throw new IllegalArgumentException("noncontiguous history"); cursor=id; }
        var unique=new LinkedHashMap<String,CAP108Seam.Adjustment>();
        for(var e:published) unique.putIfAbsent(e.effectIdentity(),e);
        return new CAP108Seam.BackfillResult(cursor,ids.size(),live,0,null,new ArrayList<>(unique.values()));
    }
    @Test void independentLanePlanReservesTwoLiveSlotsAndOneBackfillSlot() {
        var jobs=List.of(new CAP108Seam.Job("l1",CAP108Seam.Lane.LIVE),new CAP108Seam.Job("l2",CAP108Seam.Lane.LIVE),new CAP108Seam.Job("b1",CAP108Seam.Lane.BACKFILL),new CAP108Seam.Job("b2",CAP108Seam.Lane.BACKFILL));
        var p=oraclePlan(jobs); assertEquals(3,p.running().size()); assertEquals(1,p.queued().size());
        assertEquals(2,p.running().stream().filter(j->j.lane()==CAP108Seam.Lane.LIVE).count()); assertEquals(1,p.running().stream().filter(j->j.lane()==CAP108Seam.Lane.BACKFILL).count());
    }
    @Test void successfulHistoricalBatchLeavesLiveCursorAndMoneyUntouched() {
        var r=oracle(40,120,java.util.stream.LongStream.rangeClosed(41,50).boxed().toList(),null,List.of());
        assertEquals(50,r.backfillCursor());assertEquals(10,r.staged());assertEquals(120,r.liveCursor());assertEquals(0,r.moneyActions());
    }
    @Test void failedBatchKeepsPublishedDiagnosticsAndCarriesRunIdentity() {
        var old=List.of(new CAP108Seam.Adjustment("refund:x","REFUND","PENDING","r1",900));
        var r=oracle(40,120,java.util.stream.LongStream.rangeClosed(41,50).boxed().toList(),45L,old);
        assertEquals(old,r.diagnostics());assertEquals(40,r.backfillCursor());assertEquals("cap108-run-45",r.failedRunId());assertEquals(120,r.liveCursor());
    }
    @Test void revisionChangeCannotReleasePendingRefundAllocation() {
        var before=new CAP108Seam.Adjustment("refund:p:r7","REFUND","PENDING","r7",1200);
        var after=new CAP108Seam.Adjustment("refund:p:r7","REFUND","PENDING","r8",1200);
        assertEquals(before.effectIdentity(),after.effectIdentity());assertEquals(1200,after.reservedMinor());assertEquals("PENDING",after.status());
    }
    @Test void liveAndHistoricalCopiesHaveOneLogicalEffectKey() {
        var rows=List.of(new CAP108Seam.Adjustment("capture:p:a1","CAPTURE","CONFIRMED","r1",0),new CAP108Seam.Adjustment("capture:p:a1","CAPTURE","CONFIRMED","r1",0));
        var r=oracle(40,120,List.of(41L),null,rows); assertEquals(1,r.diagnostics().size());assertEquals(0,r.moneyActions());
    }
}
