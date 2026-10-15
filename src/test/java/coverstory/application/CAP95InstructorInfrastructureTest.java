package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.application.BoundOptionalAssistanceRequests.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Independent test-only policy oracle. It does not invoke the learner seam. */
class CAP95InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-95/synthetic-v1.json");

    @Test void fixtureIsSyntheticAndContainsTheFiveSavedCases() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_model_call").asBoolean());
        assertEquals(2000, root.path("limits").path("max_input_tokens").asInt());
        assertEquals(256, root.path("limits").path("max_output_tokens").asInt());
        assertEquals(1500, root.path("limits").path("deadline_ms").asInt());
        assertEquals(5, root.path("cases").size());
    }

    @Test void independentReferenceAllowsBoundaryInputAndEnforcesOutputCap() {
        AtomicInteger calls = new AtomicInteger(); AtomicReference<Integer> sentCap = new AtomicReference<>();
        ManualState manual = new ManualState("manual", "draft-A", false);
        Result accepted = reference(new AuthorizedContext("A", "q", 2000), new Limits(2000, 256, 1500L), manual,
                (ctx, cap, cancel) -> { calls.incrementAndGet(); sentCap.set(cap); return new ModelResult("grounded", 256, 1499, true); });
        assertEquals(Outcome.ACCEPTED, accepted.outcome()); assertEquals(1, calls.get());
        assertEquals(256, sentCap.get()); assertEquals(manual, accepted.manualState());
        Result oversizeOutput = reference(new AuthorizedContext("A", "q", 2000), new Limits(2000, 256, 1500L), manual,
                (ctx, cap, cancel) -> new ModelResult("unbounded", 257, 1499, true));
        assertEquals(Outcome.MALFORMED, oversizeOutput.outcome());
    }

    @Test void independentReferenceRejectsOversizeBeforeCallAndCancelsAtDeadline() {
        AtomicInteger calls = new AtomicInteger();
        FakeModelPort port = (ctx, cap, cancel) -> { calls.incrementAndGet(); return new ModelResult("late", 1, 1500, true); };
        ManualState state = new ManualState("keep me", "draft-v2", true);
        Result large = reference(new AuthorizedContext("A", "q", 2001), new Limits(2000, 256, 1500L), state, port);
        assertEquals(Outcome.LIMIT_EXCEEDED, large.outcome()); assertEquals(0, calls.get());
        AtomicReference<Cancellation> cancellation = new AtomicReference<>();
        Result late = reference(new AuthorizedContext("A", "q", 2000), new Limits(2000, 256, 1500L), state,
                (ctx, cap, token) -> { cancellation.set(token); return new ModelResult("late", 1, 1500, true); });
        assertEquals(Outcome.TIMEOUT, late.outcome()); assertTrue(cancellation.get().isCancelled());
        assertEquals(state, late.manualState());
    }

    @Test void nullOrNonpositiveLimitsFailBeforeCall() {
        AtomicInteger calls = new AtomicInteger(); FakeModelPort port = (ctx, cap, token) -> { calls.incrementAndGet(); return null; };
        var ctx = new AuthorizedContext("A", "q", 1); var state = new ManualState("m", "d", false);
        assertEquals(Outcome.INVALID_CONFIG, reference(ctx, new Limits(null, 256, 1500L), state, port).outcome());
        assertEquals(Outcome.INVALID_CONFIG, reference(ctx, new Limits(2000, null, 1500L), state, port).outcome());
        assertEquals(Outcome.INVALID_CONFIG, reference(ctx, new Limits(2000, 256, 0L), state, port).outcome());
        assertEquals(0, calls.get());
    }

    @Test void referenceIsReadOnlyAndNeverChangesManualState() {
        ManualState state = new ManualState("current form", "draft-current", true);
        Result timeout = reference(new AuthorizedContext("A", "q", 2000), new Limits(2000, 256, 1500L), state,
                (ctx, cap, token) -> new ModelResult("late", 3, 1500, true));
        assertEquals(state, timeout.manualState()); assertNull(timeout.text());
    }

    private static Result reference(AuthorizedContext context, Limits limits, ManualState manual, FakeModelPort port) {
        if (limits == null || limits.maxInputTokens() == null || limits.maxOutputTokens() == null || limits.deadlineMillis() == null
                || limits.maxInputTokens() <= 0 || limits.maxOutputTokens() <= 0 || limits.deadlineMillis() <= 0)
            return new Result(Outcome.INVALID_CONFIG, null, 0, manual);
        if (context == null || context.inputTokens() < 0) return new Result(Outcome.INVALID_CONFIG, null, 0, manual);
        if (context.inputTokens() > limits.maxInputTokens()) return new Result(Outcome.LIMIT_EXCEEDED, null, 0, manual);
        final class Token implements Cancellation { boolean cancelled; public void cancel(){cancelled=true;} public boolean isCancelled(){return cancelled;} }
        Token token = new Token();
        ModelResult response;
        try { response = port.request(context, limits.maxOutputTokens(), token); }
        catch (RuntimeException ex) { return new Result(Outcome.MALFORMED, null, 0, manual); }
        if (response == null || response.completedAtMillis() >= limits.deadlineMillis()) {
            token.cancel(); return new Result(Outcome.TIMEOUT, null, 0, manual);
        }
        if (response.outputTokens() < 0 || response.outputTokens() > limits.maxOutputTokens() || !response.grounded()
                || response.text() == null || response.text().isBlank())
            return new Result(Outcome.MALFORMED, null, 0, manual);
        return new Result(Outcome.ACCEPTED, response.text(), response.outputTokens(), manual);
    }
}
