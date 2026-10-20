package assignments.reboot;

import assignments.insurance.QuoteExpiredException;
import assignments.insurance.QuoteFreshnessPolicy;
import assignments.insurance.QuoteFreshnessState;
import assignments.insurance.QuoteSelectionFreshnessGuard;
import assignments.insurance.QuoteVersionStaleException;
import assignments.insurance.ResourceNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/** Day 11 tests only QuoteFreshnessPolicy; metadata and the write callback are fixtures. */
@Tag("learnerAcceptance")
class Day11ContractTest {
    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");
    private final QuoteFreshnessPolicy policy = new QuoteFreshnessPolicy();

    private QuoteFreshnessState state(String requestedJourney, String quoteJourney,
            String requestedVersion, String currentVersion, Instant expiresAt) {
        return new QuoteFreshnessState(requestedJourney, quoteJourney,
                requestedVersion, currentVersion, expiresAt);
    }

    @Test
    void currentUnexpiredVersionIsSelectable() {
        assertDoesNotThrow(() -> policy.assertSelectable(
                state("journey-1", "journey-1", "v2", "v2", NOW.plusSeconds(1)), NOW));
    }

    @Test
    void expiredAtNowIsRejectedBeforeAnyWrite() {
        var writes = new AtomicInteger();
        var guard = new QuoteSelectionFreshnessGuard(policy);
        assertThrows(QuoteExpiredException.class, () -> guard.select(
                state("journey-1", "journey-1", "v2", "v2", NOW), NOW, writes::incrementAndGet));
        assertEquals(0, writes.get());
    }

    @Test
    void supersededVersionIsRejectedBeforeAnyWrite() {
        var writes = new AtomicInteger();
        var guard = new QuoteSelectionFreshnessGuard(policy);
        assertThrows(QuoteVersionStaleException.class, () -> guard.select(
                state("journey-1", "journey-1", "v1", "v2", NOW.plusSeconds(60)), NOW,
                writes::incrementAndGet));
        assertEquals(0, writes.get());
    }

    @Test
    void anotherJourneysQuoteIsNonDisclosingAndDoesNotWrite() {
        var writes = new AtomicInteger();
        var guard = new QuoteSelectionFreshnessGuard(policy);
        assertThrows(ResourceNotFoundException.class, () -> guard.select(
                state("journey-1", "journey-2", "v2", "v2", NOW.plusSeconds(60)), NOW,
                writes::incrementAndGet));
        assertEquals(0, writes.get());
    }
}
