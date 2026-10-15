package capstone.coverstory.application;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CAP111InstructorInfrastructureTest {
    @Test void explicitReviewGateReturnsPendingWithoutDispatch() {
        var facts = new CAP111InstructorOracle.Facts(3,3,
                java.util.UUID.fromString("11100000-0000-4000-8000-000000000004"),
                java.util.UUID.fromString("11100000-0000-4000-8000-000000000004"),
                "a".repeat(64), "a".repeat(64), "b".repeat(64), "b".repeat(64), true,true,true,true,true);
        var accepted = CAP111InstructorOracle.evaluate(facts, true);
        assertEquals(202, accepted.status()); assertEquals("SUBMISSION_PENDING", accepted.code());
        assertFalse(accepted.dispatchCreated());
        assertEquals("EXPLICIT_CONSENT_REQUIRED", CAP111InstructorOracle.evaluate(facts, false).code());
    }

    @Test void staleReviewAndUnresolvedFinanceAreBlocked() {
        var quote = java.util.UUID.randomUUID();
        var current = new CAP111InstructorOracle.Facts(4,3,quote,quote,
                "a".repeat(64),"a".repeat(64),"b".repeat(64),"b".repeat(64),true,true,true,true,true);
        assertEquals("REVIEW_STALE", CAP111InstructorOracle.evaluate(current,true).code());
        var financeQuote = java.util.UUID.randomUUID();
        var finance = new CAP111InstructorOracle.Facts(3,3,financeQuote,financeQuote,
                "a".repeat(64),"a".repeat(64),"b".repeat(64),"b".repeat(64),true,true,true,false,true);
        assertEquals("REVIEW_BLOCKED", CAP111InstructorOracle.evaluate(finance,true).code());
    }

    @Test void capacityIsReproducibleButNeverProof() {
        var estimate = CAP111CapacityOracle.estimate(1_000_000,30,.01,60,.95);
        assertEquals(33333.33, estimate.browserRps()); assertEquals(1666.67, estimate.originRps());
        assertEquals(166.67, estimate.transactionRps()); assertFalse(estimate.capacityProven());
        assertEquals("not_benchmarked", estimate.caveats().get("measurement"));
        var noCache = CAP111CapacityOracle.estimate(1_000_000,30,.01,60,0);
        assertEquals(33333.33, noCache.originRps()); assertFalse(noCache.capacityProven());
    }
}
