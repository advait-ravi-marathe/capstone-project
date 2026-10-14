package capstone.coverstory.payments;

import capstone.coverstory.payments.api.CAP169Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Learner-owned behavior checks; expected to fail until the seam TODO is implemented. */
@Tag("learnerAcceptance")
class CAP169Test {
    private final CAP169Seam seam = new CAP169Seam();
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);
    private final Duration maxWait = Duration.ofSeconds(30);

    @Test void parsesWholeSeconds() {
        assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.WAIT, Duration.ofSeconds(10)),
                seam.classifyRetryAfter("10", fixedClock, maxWait));
    }

    @Test void parsesHttpDateUsingInjectedClock() {
        assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.WAIT, Duration.ofSeconds(20)),
                seam.classifyRetryAfter("Wed, 14 Oct 2026 12:00:20 GMT", fixedClock, maxWait));
    }

    @Test void doesNotClampLongWaitIntoAnEarlyRetry() {
        assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.STOP_LONG_DELAY, null),
                seam.classifyRetryAfter("31", fixedClock, maxWait));
    }

    @Test void pastHttpDateWaitsZero() {
        assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.WAIT, Duration.ZERO),
                seam.classifyRetryAfter("Wed, 14 Oct 2026 11:59:55 GMT", fixedClock, maxWait));
    }

    @Test void distinguishesMalformedFromAbsent() {
        for (String invalid : new String[]{"-1", "1.5", "nonsense"})
            assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.INVALID_HEADER, null),
                    seam.classifyRetryAfter(invalid, fixedClock, maxWait));
        assertEquals(new CAP169Seam.Decision(CAP169Seam.Status.ABSENT, null),
                seam.classifyRetryAfter(null, fixedClock, maxWait));
    }
}
