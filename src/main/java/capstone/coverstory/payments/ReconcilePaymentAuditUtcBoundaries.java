package capstone.coverstory.payments;

import coverstory.modules.payments.api.PaymentAuditWindowReadPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** CAP-173 learner seam: reconcile an owner-scoped audit count over [startInclusive, endExclusive). */
public final class ReconcilePaymentAuditUtcBoundaries implements CAP173AuditAction {
    public enum Status { OK, UNAUTHENTICATED, INVALID_RANGE }
    public record IdsAndCount(List<UUID> ids, int count) {
        public IdsAndCount { ids = List.copyOf(ids); }
    }
    public record Reply(Status status, IdsAndCount result) { }

    private final PaymentAuditWindowReadPort audit;

    public ReconcilePaymentAuditUtcBoundaries(PaymentAuditWindowReadPort audit) { this.audit = audit; }

    @Override public Reply select(UUID authenticatedOwner, Instant startInclusive, Instant endExclusive) {
        // LEARNER TODO: reject a missing owner and null/empty/reversed interval before any SQL call;
        // call the Payments-owned reader with the trusted owner and aware instants; return the
        // sorted, unique journal IDs and a count reconciled to that exact ID list. The range is
        // inclusive at start and exclusive at end. Do not use session-local date boundaries.
        throw new UnsupportedOperationException("CAP-173 learner TODO: reconcile owner-scoped UTC audit window");
    }
}
