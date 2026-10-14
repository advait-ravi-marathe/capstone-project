package coverstory.application;

import java.util.Objects;

/** CAP-95 policy seam. The port is an offline teaching fake, never a provider SDK. */
public final class BoundOptionalAssistanceRequests {
    public record Limits(Integer maxInputTokens, Integer maxOutputTokens, Long deadlineMillis) { }
    public record AuthorizedContext(String ownerId, String question, int inputTokens) { }
    public record ManualState(String formValue, String draftId, boolean consented) { }
    /** completedAtMillis is read from the injected monotonic teaching clock. */
    public record ModelResult(String text, int outputTokens, long completedAtMillis, boolean grounded) { }
    public enum Outcome { ACCEPTED, LIMIT_EXCEEDED, TIMEOUT, INVALID_CONFIG, MALFORMED }
    public record Result(Outcome outcome, String text, int outputTokens, ManualState manualState) { }

    @FunctionalInterface public interface FakeModelPort {
        ModelResult request(AuthorizedContext context, int maxOutputTokens, Cancellation cancellation);
    }
    public interface Cancellation { void cancel(); boolean isCancelled(); }
    @FunctionalInterface public interface MonotonicClock { long nowMillis(); }

    private final FakeModelPort port;
    private final MonotonicClock clock;
    public BoundOptionalAssistanceRequests(FakeModelPort port, MonotonicClock clock) {
        this.port = Objects.requireNonNull(port);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Learner TODO: validate positive configured limits before calling the port; reject input over
     * maxInputTokens; set deadline = clock.nowMillis() + deadlineMillis; call the port exactly once
     * with maxOutputTokens; cancel and return TIMEOUT when completion is at/after deadline; reject
     * ungrounded, blank, or over-cap responses; preserve manualState verbatim in every outcome.
     */
    public Result requestAssistance(AuthorizedContext context, Limits limits, ManualState manualState) {
        throw new UnsupportedOperationException("CAP-95 learner TODO: bound optional assistance");
    }
}
