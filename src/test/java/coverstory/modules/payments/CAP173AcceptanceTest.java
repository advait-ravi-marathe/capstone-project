package coverstory.modules.payments;

import capstone.coverstory.payments.ReconcilePaymentAuditUtcBoundaries;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Saved learner acceptance contract; these checks intentionally stop at the preserved TODO. */
@Tag("learnerAcceptance")
class CAP173AcceptanceTest {
    private static final UUID A = UUID.fromString("17300000-0000-4000-8000-000000000001");
    private static final Instant START = Instant.parse("2026-12-15T00:00:00Z");
    private static final Instant END = Instant.parse("2026-12-16T00:00:00Z");
    private static final List<UUID> IDS = List.of(UUID.fromString("17300000-0000-4000-8000-000000000102"),
            UUID.fromString("17300000-0000-4000-8000-000000000103"));
    private final AtomicInteger reads = new AtomicInteger();
    private final ReconcilePaymentAuditUtcBoundaries seam = new ReconcilePaymentAuditUtcBoundaries((owner, start, end) -> {
        reads.incrementAndGet(); return IDS;
    });

    @Test void includesStartAndExcludesEndAndReconcilesCount() {
        var result = seam.select(A, START, END);
        assertEquals(IDS, result.result().ids()); assertEquals(2, result.result().count());
    }

    @Test void equivalentOffsetRepresentsTheSameInstant() {
        assertEquals(IDS, seam.select(A, Instant.parse("2026-12-15T05:30:00+05:30"),
                Instant.parse("2026-12-16T05:30:00+05:30")).result().ids());
    }

    @Test void invalidRangeIsRejectedBeforeDatabaseRead() {
        assertEquals(ReconcilePaymentAuditUtcBoundaries.Status.INVALID_RANGE,
                seam.select(A, END, START).status()); assertEquals(0, reads.get());
    }

    @Test void missingOwnerIsRejectedBeforeDatabaseRead() {
        assertEquals(ReconcilePaymentAuditUtcBoundaries.Status.UNAUTHENTICATED,
                seam.select(null, START, END).status()); assertEquals(0, reads.get());
    }

    @Test void countsRemainOwnerScoped() {
        UUID B = UUID.fromString("17300000-0000-4000-8000-000000000002");
        assertEquals(1, seam.select(B, START, END).result().count());
    }
}
