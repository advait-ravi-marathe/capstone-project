package coverstory.modules.payments.api;

import java.util.List;

/** Local, isolated planning seam for adjustment-audit recomputation. No money or live-lane port. */
public final class CAP108Seam {
    public enum Lane { LIVE, BACKFILL }
    public record Job(String id, Lane lane) { }
    public record Plan(List<Job> running, List<Job> queued) {
        public Plan { running = List.copyOf(running); queued = List.copyOf(queued); }
    }
    public record Adjustment(String effectIdentity, String kind, String status,
                             String revision, long reservedMinor) { }
    public record BackfillResult(long backfillCursor, int staged, long liveCursor,
                                 int moneyActions, String failedRunId,
                                 List<Adjustment> diagnostics) {
        public BackfillResult { diagnostics = List.copyOf(diagnostics); }
    }

    /** Learner task: implement bounded local recomputation and isolated lane scheduling. */
    public Plan plan(List<Job> arrivals) {
        throw new UnsupportedOperationException("TODO: cap108 isolated lane planning");
    }

    /** Learner task: recompute diagnostics only; never call a payment/provider action. */
    public BackfillResult recompute(long startingBackfillCursor, long liveCursor,
                                    List<Long> ids, Long failAt,
                                    List<Adjustment> publishedDiagnostics) {
        throw new UnsupportedOperationException("TODO: cap108 audit recomputation");
    }
}
