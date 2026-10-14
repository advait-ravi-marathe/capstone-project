package coverstory.modules.application;

import coverstory.modules.application.api.CAP106Seam;
import coverstory.modules.application.api.CAP106Seam.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-facing TODO contract: these failures are expected before implementation. */
@Tag("learnerAcceptance")
class CAP106AcceptanceTest {
    private static final Principal A = new Principal("customer-A", "READER");
    private static final Principal B = new Principal("customer-B", "READER");
    private static final Owner OWNER_A = new Owner("customer-A");
    private static final Key R1 = new Key("owners/customer-A/applications/application-A/revisions/r1.json", "customer-A", "application-A", "r1", RevisionKind.HISTORICAL);
    private static final Key R2 = new Key("owners/customer-A/applications/application-A/revisions/r2.json", "customer-A", "application-A", "r2", RevisionKind.CURRENT);

    private Harness harness() {
        AtomicInteger reads = new AtomicInteger();
        List<Key> fetched = new ArrayList<>();
        CAP106Seam seam = new CAP106Seam(key -> { reads.incrementAndGet(); fetched.add(key); return new Snapshot(key.revisionId(), key.kind(), "synthetic details " + key.revisionId()); });
        return new Harness(seam, reads, fetched);
    }

    @Test void ownTlsGetReadsExactlyOneLocalSnapshot() {
        Harness h = harness(); Snapshot result = h.seam.read(A, Operation.GET, OWNER_A, R1, Transport.TLS);
        assertNotNull(result); assertEquals(1, h.reads.get());
        assertEquals("r1", result.revisionId()); assertEquals(RevisionKind.HISTORICAL, result.kind());
    }

    @Test void foreignOwnerIsDeniedBeforeAnyReadAndHasNoMetadata() {
        Harness h = harness(); Snapshot result = h.seam.read(B, Operation.GET, OWNER_A, R1, Transport.TLS);
        assertNull(result); assertEquals(0, h.reads.get()); assertTrue(h.fetched.isEmpty());
    }

    @Test void putAndClearTextAreDeniedWithoutReadsOrMutationPort() {
        Harness h = harness();
        assertNull(h.seam.read(A, Operation.PUT, OWNER_A, R1, Transport.TLS));
        assertNull(h.seam.read(A, Operation.GET, OWNER_A, R1, Transport.CLEAR_TEXT));
        assertEquals(0, h.reads.get());
    }

    @Test void unknownTraversalAndNullOwnerDenyBeforeFetch() {
        Harness h = harness();
        Key unknown = new Key("owners/customer-A/../../customer-B.json", "customer-A", "application-A", "rX", RevisionKind.HISTORICAL);
        assertNull(h.seam.read(A, Operation.GET, OWNER_A, unknown, Transport.TLS));
        assertNull(h.seam.read(A, Operation.GET, null, R1, Transport.TLS));
        assertEquals(0, h.reads.get());
    }

    @Test void historicAndCurrentSnapshotsRemainVersionLabelled() {
        Harness h = harness();
        Snapshot historic = h.seam.read(A, Operation.GET, OWNER_A, R1, Transport.TLS);
        Snapshot current = h.seam.read(A, Operation.GET, OWNER_A, R2, Transport.TLS);
        assertNotNull(historic); assertNotNull(current);
        assertEquals(RevisionKind.HISTORICAL, historic.kind()); assertEquals("r1", historic.revisionId());
        assertEquals(RevisionKind.CURRENT, current.kind()); assertEquals("r2", current.revisionId());
        assertNotEquals(historic.revisionId(), current.revisionId());
    }

    private record Harness(CAP106Seam seam, AtomicInteger reads, List<Key> fetched) { }
}
