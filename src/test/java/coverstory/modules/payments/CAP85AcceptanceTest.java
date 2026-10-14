package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP85Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-facing acceptance contract; default Surefire excludes this group. */
@Tag("learnerAcceptance")
class CAP85AcceptanceTest {
    private static final UUID OWNER_A = UUID.fromString("85000000-0000-4000-8000-000000000001");
    private static final UUID BATCH = UUID.fromString("85000000-0000-4000-8000-000000000101");

    @Test void classifiesEveryStagedRowAndVerifiesBothInsertedEffects() {
        CAP85Seam.PaymentBatchSummary s = new CAP85Seam((o,b) -> null).summarize(OWNER_A, BATCH);
        assertEquals(new CAP85Seam.PaymentBatchSummary(4,1,1,2,0,0), s);
    }

    @Test void reportsAnUnaccountedStagedRow() {
        CAP85Seam.PaymentBatchSummary s = new CAP85Seam((o,b) -> null).summarize(OWNER_A, BATCH);
        assertEquals(1, s.unaccountedCount());
    }

    @Test void emptyOwnedBatchReturnsOneZeroSummary() {
        CAP85Seam.PaymentBatchSummary s = new CAP85Seam((o,b) -> null).summarize(OWNER_A, BATCH);
        assertEquals(new CAP85Seam.PaymentBatchSummary(0,0,0,0,0,0), s);
    }

    @Test void sameBatchIdForAnotherOwnerDoesNotChangeSummary() {
        CAP85Seam.PaymentBatchSummary s = new CAP85Seam((o,b) -> null).summarize(OWNER_A, BATCH);
        assertEquals(4, s.rawCount());
    }

    @Test void arithmeticBalanceCannotHideMissingInsertedLineage() {
        CAP85Seam.PaymentBatchSummary s = new CAP85Seam((o,b) -> null).summarize(OWNER_A, BATCH);
        assertEquals(1, s.missingInsertedEffectCount());
    }
}
