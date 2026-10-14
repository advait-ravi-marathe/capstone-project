package capstone.coverstory.shortlist;

import capstone.coverstory.shortlist.api.CAP68Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Saved learner cases; implementation remains intentionally TODO. */
@Tag("learnerAcceptance")
class CAP68Test {
    private final CAP68Seam seam = new CAP68Seam();
    private final CAP68Seam.Command command = new CAP68Seam.Command(
            UUID.fromString("68000000-0000-4000-8000-000000000002"),
            UUID.fromString("68000000-0000-4000-8000-000000000001"),
            UUID.fromString("68000000-0000-4000-8000-000000000004"),
            filled((byte) 17), filled((byte) 34), 1);
    private final CAP68Seam.StoredResult committed = new CAP68Seam.StoredResult(201, 2, command.operationId());

    @Test void normalCommitHasKnownCreatedResponseAndVersion() {
        CAP68Seam.GapReport report = run(() -> { }, true);
        assertFalse(report.clientStateUnknown());
        assertTrue(report.durableCommitObserved());
        assertEquals(2, report.committedVersion());
        assertFalse(report.notificationGap());
    }

    @Test void lostResponseKeepsClientUnknownUntilDurableStateIsRecovered() {
        CAP68Seam.GapReport report = run(() -> { throw new LostResponse(); }, true);
        assertTrue(report.durableCommitObserved());
        assertTrue(report.clientStateUnknown());
        assertEquals(2, report.committedVersion());
    }

    @Test void recoveryReusesTheOperationAndOwnerScopedReadDoesNotCrossOwners() {
        AtomicInteger commits = new AtomicInteger();
        CAP68Seam.GapReport report = seam.crashAfterShortlistCommitBeforeResponse(
                command,
                c -> { commits.incrementAndGet(); return committed; },
                (owner, journey) -> owner.equals(command.ownerId()) && journey.equals(command.journeyId())
                        ? Optional.of(committed) : Optional.empty(),
                () -> { throw new LostResponse(); },
                ignored -> true);
        assertEquals(1, commits.get(), "recovery must not create a second shortlist effect");
        assertEquals(2, report.committedVersion());
        assertTrue("SAME_KEY_REPLAY".equals(report.recoverySource()) || "OWNER_SCOPED_GET".equals(report.recoverySource()));
    }

    @Test void missingOptionalPublicationIsReportedApartFromTheCommittedAudit() {
        CAP68Seam.GapReport report = run(() -> { throw new LostResponse(); }, false);
        assertTrue(report.durableCommitObserved());
        assertTrue(report.notificationGap());
        assertTrue(report.gaps().contains("OPTIONAL_NOTIFICATION_NOT_DELIVERED"));
    }

    private CAP68Seam.GapReport run(CAP68Seam.ResponseLossHook responseHook, boolean published) {
        return seam.crashAfterShortlistCommitBeforeResponse(command,
                ignored -> committed,
                (owner, journey) -> Optional.of(committed),
                responseHook,
                ignored -> published);
    }

    private static byte[] filled(byte value) { byte[] bytes = new byte[32]; java.util.Arrays.fill(bytes, value); return bytes; }
    private static final class LostResponse extends Exception { }
}
