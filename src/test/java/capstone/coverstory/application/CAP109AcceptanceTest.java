package capstone.coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP109AcceptanceTest {
    private static final DiffAndReviewAcceptedQuoteRevisions.Owner OWNER = new DiffAndReviewAcceptedQuoteRevisions.Owner(UUID.fromString("10900000-0000-4000-8000-000000000002"));
    private static DiffAndReviewAcceptedQuoteRevisions.ReviewSnapshot snapshot(long revision, String version, long total, String terms, Map<String, String> digests) {
        return new DiffAndReviewAcceptedQuoteRevisions.ReviewSnapshot(UUID.fromString("10900000-0000-4000-8000-000000000001"), revision,
                version, "synthetic://envelope/app-1/revision-" + revision, digests, total, "INR", terms, List.of(), revision, true, true);
    }

    @Test void reportsOnlySourceLinkedChangedAddedAndRemovedDigestFields() {
        var delta = DiffAndReviewAcceptedQuoteRevisions.diffAndCheckAudit(OWNER,
                snapshot(1, "v1", 100000, "terms-a", Map.of("plan", "a1", "rider", "b1")),
                snapshot(2, "v2", 110000, "terms-b", Map.of("plan", "a2", "option", "c1")));
        assertEquals(Map.of("plan", new DiffAndReviewAcceptedQuoteRevisions.FieldChange("a1", "a2")), delta.changed());
        assertEquals(List.of("option"), delta.added()); assertEquals(List.of("rider"), delta.removed());
        assertEquals(10000, delta.amountDeltaMinor()); assertTrue(delta.explicitReviewRequired());
    }

    @Test void returnsNoChangesForTheSameReviewedRevision() {
        var v = snapshot(2, "v2", 100000, "terms-a", Map.of("plan", "a2"));
        var delta = DiffAndReviewAcceptedQuoteRevisions.diffAndCheckAudit(OWNER, v, v);
        assertTrue(delta.changed().isEmpty() && delta.added().isEmpty() && delta.removed().isEmpty());
    }

    @Test void requiresReviewForChangedTermsEvenWhenPriceIsUnchanged() {
        var delta = DiffAndReviewAcceptedQuoteRevisions.diffAndCheckAudit(OWNER,
                snapshot(1, "v1", 100000, "terms-a", Map.of("plan", "a1")),
                snapshot(2, "v2", 100000, "terms-b", Map.of("plan", "a1")));
        assertEquals(0, delta.amountDeltaMinor()); assertTrue(delta.explicitReviewRequired());
    }

    @Test void refusesSnapshotsFromAnotherOwner() {
        var other = new DiffAndReviewAcceptedQuoteRevisions.Owner(UUID.fromString("10900000-0000-4000-8000-000000000003"));
        assertThrows(UnsupportedOperationException.class, () -> DiffAndReviewAcceptedQuoteRevisions.diffAndCheckAudit(other,
                snapshot(1, "v1", 100000, "terms-a", Map.of()), snapshot(2, "v2", 110000, "terms-b", Map.of())));
    }
}
