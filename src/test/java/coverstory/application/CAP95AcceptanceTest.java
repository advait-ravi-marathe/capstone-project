package coverstory.application;

import coverstory.application.BoundOptionalAssistanceRequests.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP95AcceptanceTest {
    private static final Limits LIMITS = new Limits(2000, 256, 1500L);
    private static final AuthorizedContext CONTEXT = new AuthorizedContext("owner-A", "synthetic question", 2000);
    private static final ManualState MANUAL = new ManualState("typed text", "draft-A-v1", false);

    @Test void maxInputFinishingBeforeDeadlineMakesOneCappedCall() {
        AtomicInteger calls = new AtomicInteger();
        BoundOptionalAssistanceRequests seam = new BoundOptionalAssistanceRequests((c, cap, cancel) -> {
            calls.incrementAndGet(); assertEquals(256, cap);
            return new ModelResult("Grounded synthetic explanation", 256, 1499, true);
        }, () -> 0L);
        Result result = seam.requestAssistance(CONTEXT, LIMITS, MANUAL);
        assertEquals(1, calls.get()); assertEquals(Outcome.ACCEPTED, result.outcome());
        assertEquals(256, result.outputTokens()); assertEquals(MANUAL, result.manualState());
    }

    @Test void overLimitInputDoesNotCallPort() {
        AtomicInteger calls = new AtomicInteger();
        var context = new AuthorizedContext("owner-A", "too large", 2001);
        var seam = new BoundOptionalAssistanceRequests((c, cap, cancel) -> { calls.incrementAndGet(); return null; }, () -> 0L);
        assertEquals(Outcome.LIMIT_EXCEEDED, seam.requestAssistance(context, LIMITS, MANUAL).outcome());
        assertEquals(0, calls.get());
    }

    @Test void deadlineCancelsSingleAttemptWithoutRetry() {
        AtomicInteger calls = new AtomicInteger(); AtomicReference<Cancellation> token = new AtomicReference<>();
        var seam = new BoundOptionalAssistanceRequests((c, cap, cancellation) -> {
            calls.incrementAndGet(); token.set(cancellation); return new ModelResult("late", 1, 1500, true);
        }, () -> 0L);
        Result result = seam.requestAssistance(CONTEXT, LIMITS, new ManualState("changed meanwhile", "draft-A-v2", true));
        assertEquals(Outcome.TIMEOUT, result.outcome());
        assertEquals(1, calls.get()); assertNotNull(token.get()); assertTrue(token.get().isCancelled());
        assertEquals(new ManualState("changed meanwhile", "draft-A-v2", true), result.manualState());
    }

    @Test void invalidConfigurationFailsBeforePortCall() {
        AtomicInteger calls = new AtomicInteger();
        var seam = new BoundOptionalAssistanceRequests((c, cap, token) -> { calls.incrementAndGet(); return null; }, () -> 0L);
        assertEquals(Outcome.INVALID_CONFIG, seam.requestAssistance(CONTEXT, new Limits(null, 256, 1500L), MANUAL).outcome());
        assertEquals(Outcome.INVALID_CONFIG, seam.requestAssistance(CONTEXT, new Limits(2000, null, 1500L), MANUAL).outcome());
        assertEquals(Outcome.INVALID_CONFIG, seam.requestAssistance(CONTEXT, new Limits(2000, 256, 0L), MANUAL).outcome());
        assertEquals(0, calls.get());
    }

    @Test void timeoutDuringFormUsePreservesManualValueDraftAndConsent() {
        var current = new ManualState("new user entry", "draft-A-v2", true);
        var seam = new BoundOptionalAssistanceRequests((c, cap, token) -> new ModelResult("late", 1, 1500, true), () -> 0L);
        Result result = seam.requestAssistance(CONTEXT, LIMITS, current);
        assertEquals(Outcome.TIMEOUT, result.outcome()); assertEquals(current, result.manualState());
    }
}
