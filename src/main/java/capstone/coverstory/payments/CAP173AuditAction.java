package capstone.coverstory.payments;

import java.time.Instant;
import java.util.UUID;

/** HTTP-facing action contract. Production configuration delegates to the preserved learner seam. */
@FunctionalInterface
public interface CAP173AuditAction {
    ReconcilePaymentAuditUtcBoundaries.Reply select(UUID owner, Instant startInclusive, Instant endExclusive);
}
