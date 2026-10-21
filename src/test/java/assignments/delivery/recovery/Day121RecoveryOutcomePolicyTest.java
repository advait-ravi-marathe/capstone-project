package assignments.delivery.recovery;

import assignments.delivery.IssuanceEnvelope;
import assignments.delivery.IssuanceSourceKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class Day121RecoveryOutcomePolicyTest {
    private final RecoveryOutcomePolicy policy = new RecoveryOutcomePolicy();
    private final IssuanceEnvelope envelope = new IssuanceEnvelope("policy-a", 1, "ISSUED", LocalDate.parse("2026-12-01"), 1);

    @Test void mapsOnlyObservedEvidenceAfterACompletedAttempt() {
        assertEquals(RecoveryOutcome.DELIVERED, policy.afterAcknowledgment(observation(null, ReceiptRelation.ABSENT)));
        assertEquals(RecoveryOutcome.RECONCILED, policy.afterAcknowledgment(observation(null, ReceiptRelation.MATCHING)));
        assertEquals(RecoveryOutcome.ALREADY_COMPLETE, policy.afterAcknowledgment(observation(Instant.EPOCH, ReceiptRelation.MATCHING)));
        assertThrows(RecoveryPreconditionException.class,
                () -> policy.afterAcknowledgment(observation(Instant.EPOCH, ReceiptRelation.ABSENT)));
        assertThrows(RecoveryPreconditionException.class,
                () -> policy.afterAcknowledgment(observation(null, ReceiptRelation.CONFLICTING)));
    }

    private RecoveryObservation observation(Instant acknowledged, ReceiptRelation receipt) {
        return new RecoveryObservation(envelope.sourceKey(), true, envelope, 0, acknowledged, null, null, receipt);
    }
}
