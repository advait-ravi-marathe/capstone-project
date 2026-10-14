package capstone.coverstory.payments.api;

import java.time.Clock;
import java.time.Duration;

/** CAP-169 learner seam: classify a Retry-After value without authorizing a retry. */
public final class CAP169Seam {
    public enum Status { WAIT, STOP_LONG_DELAY, INVALID_HEADER, ABSENT }

    public record Decision(Status status, Duration delay) { }

    /**
     * Parse a nonnegative whole-seconds value or an HTTP date using the supplied
     * clock. A delay over maxWait is deferred; it must never be shortened.
     */
    public Decision classifyRetryAfter(String rawHeader, Clock fixedClock, Duration maxWait) {
        // LEARNER TODO: implement strict seconds / HTTP-date parsing, distinguish
        // absent from malformed values, clamp past dates to WAIT(0), and return
        // STOP_LONG_DELAY when the valid delay exceeds maxWait. This parser does
        // not decide if an operation is safe to retry and has no payment effects.
        throw new UnsupportedOperationException("CAP-169 learner TODO: classify Retry-After");
    }
}
