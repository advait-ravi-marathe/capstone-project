package assignments.learning;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Day07TimeReferenceTest {
    record Reservation(String rateVersion, String capturedPrice, Instant expiresAt) { }
    static boolean validAt(Instant now, Instant expiresAt) { return now.isBefore(expiresAt); }

    @Test void fixedClockDefinesStrictExpiryAndCapturedValue() {
        var created = Instant.parse("2026-01-01T10:00:00Z");
        var clock = Clock.fixed(created, ZoneOffset.UTC);
        var observed = clock.instant();
        var expires = observed.plusSeconds(30);
        assertEquals(Instant.parse("2026-01-01T10:00:30Z"), expires);
        assertTrue(expires.minusNanos(1).isBefore(expires));
        assertFalse(expires.isBefore(expires));
        assertFalse(expires.plusNanos(1).isBefore(expires));
        assertTrue(validAt(expires.minusNanos(1), expires));
        assertFalse(validAt(expires, expires));
        assertFalse(validAt(expires.plusNanos(1), expires));
        var saved = new Reservation("rate-A", "10.00", expires);
        var currentCatalogPrice = "11.00";
        assertEquals("10.00", saved.capturedPrice());
        assertEquals("11.00", currentCatalogPrice);
    }

    @Test void expiryCanCrossUtcDayWithoutWaiting() {
        var created = Instant.parse("2026-01-01T23:59:50Z");
        var expires = Clock.fixed(created, ZoneOffset.UTC).instant().plusSeconds(30);
        assertEquals(Instant.parse("2026-01-02T00:00:20Z"), expires);
        assertTrue(expires.minusNanos(1).isBefore(expires));
        assertFalse(expires.isBefore(expires));
    }
}
