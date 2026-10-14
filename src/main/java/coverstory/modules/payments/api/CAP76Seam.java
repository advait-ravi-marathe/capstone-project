package coverstory.modules.payments.api;

import java.time.Duration;

/** CAP-76 bounded retry policy for idempotent mock-provider status GETs only. */
public final class CAP76Seam {
    public static final Duration DEADLINE = Duration.ofMillis(1_000);
    public static final int MAX_ATTEMPTS = 3;

    public enum Action {
        RETRY_AFTER,
        STOP_EXHAUSTED,
        STOP_PERMANENT,
        STOP_SUCCESS,
        DEFER_STATUS_READ
    }

    /** A known 2xx status is retained even when it arrives after the local deadline. */
    public record RetryDecision(Action action, Duration delay, boolean responseRetained, Integer responseStatus) { }

    public RetryDecision next(int attemptNumber, int httpStatus, Duration elapsed, Duration parsedRetryAfter) {
        // LEARNER TODO: apply success and permanent-status precedence, then
        // enforce attempt and strict elapsed-plus-delay bounds for transient
        // status GETs. Honor a valid Retry-After without retrying sooner.
        // Possible create/capture outcomes are not status reads: never retry a
        // charge here; an uncertain money effect needs reconciliation.
        throw new UnsupportedOperationException("CAP-76 learner TODO: implement bounded status-read retry decision");
    }
}
