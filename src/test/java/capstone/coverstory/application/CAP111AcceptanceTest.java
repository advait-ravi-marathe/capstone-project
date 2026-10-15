package capstone.coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP111AcceptanceTest {
    @Test void submitRequiresCurrentReviewAndProducesOnlyPendingIntent() {
        var command = new DefendExplicitSubmissionAndCapacityBoundaries.SubmitCommand(3, 3,
                UUID.fromString("11100000-0000-4000-8000-000000000004"), "a".repeat(64), "b".repeat(64), true);
        var result = DefendExplicitSubmissionAndCapacityBoundaries.submit(
                UUID.fromString("11100000-0000-4000-8000-000000000001"), command, "synthetic-idempotency-key");
        assertEquals(202, result.status());
        assertEquals("SUBMISSION_PENDING", result.code());
    }

    @Test void capacityEstimateLabelsAssumptionsAndDoesNotClaimProof() {
        var result = DefendExplicitSubmissionAndCapacityBoundaries.estimateCapacity(
                new DefendExplicitSubmissionAndCapacityBoundaries.CapacityAssumptions(1_000_000, 30, .01, 60, .95));
        assertEquals(33333.33, result.browserRps());
        assertEquals(1666.67, result.originRps());
        assertEquals(166.67, result.transactionRps());
        assertFalse(result.capacityProven());
        assertEquals("not_benchmarked", result.caveats().get("measurement"));
    }

    @Test void todoGuardKeepsBusinessAndCapacityLogicForTheLearner() {
        assertTrue(java.util.Arrays.stream(DefendExplicitSubmissionAndCapacityBoundaries.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("submit")));
        assertTrue(java.util.Arrays.stream(DefendExplicitSubmissionAndCapacityBoundaries.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("estimateCapacity")));
    }
}
