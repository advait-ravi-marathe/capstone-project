package assignments.reboot;

import assignments.insurance.EligibilityDecision;
import assignments.insurance.HomeSecureEligibilityCheck;
import assignments.insurance.PremiumQuoteCalculator;
import assignments.insurance.QuoteCreationInput;
import assignments.insurance.QuoteVersionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Day 7 tests only QuoteVersionService; earlier policies are fixed test collaborators. */
@Tag("learnerAcceptance")
class Day07ContractTest {
    private static final Instant CREATED = Instant.parse("2026-10-27T09:00:00Z");
    private static final QuoteCreationInput INPUT = new QuoteCreationInput(
            "OWNER_OCCUPIED", new BigDecimal("100000.00"), "BUILDING_CONTENTS");

    private QuoteVersionService service(boolean eligible, AtomicInteger premiumCalls) {
        HomeSecureEligibilityCheck fixedEligibility = input -> new EligibilityDecision(
                eligible, eligible ? "ELIGIBLE" : "OWNER_OCCUPIED_REQUIRED");
        PremiumQuoteCalculator fixedPricing = (amount, coverage) -> {
            premiumCalls.incrementAndGet();
            return new BigDecimal("250.13");
        };
        return new QuoteVersionService(
                "homesecure", "home-secure-v1", "home-secure-training-rate-v1", "INR",
                Duration.ofMinutes(15), fixedEligibility, fixedPricing,
                Clock.fixed(CREATED, ZoneOffset.UTC), () -> "quote-fixed-001");
    }

    @Test
    void capturesVersionsPriceAndExactFifteenMinuteExpiry() {
        var premiumCalls = new AtomicInteger();
        var quote = service(true, premiumCalls).createQuote(INPUT);
        assertEquals("quote-fixed-001", quote.quoteVersionId());
        assertEquals("home-secure-v1", quote.productVersion());
        assertEquals("home-secure-training-rate-v1", quote.rateVersion());
        assertEquals(new BigDecimal("250.13"), quote.annualPremiumRupees());
        assertEquals(CREATED, quote.createdAt());
        assertEquals(CREATED.plus(Duration.ofMinutes(15)), quote.expiresAt());
        assertEquals(1, premiumCalls.get());
    }

    @Test
    void quoteIsValidOnlyStrictlyBeforeExpiry() {
        var quote = service(true, new AtomicInteger()).createQuote(INPUT);
        assertTrue(service(true, new AtomicInteger()).isValidAt(quote, quote.expiresAt().minusNanos(1)));
        assertFalse(service(true, new AtomicInteger()).isValidAt(quote, quote.expiresAt()));
        assertFalse(service(true, new AtomicInteger()).isValidAt(quote, quote.expiresAt().plusNanos(1)));
    }

    @Test
    void ineligibleInputIsRejectedBeforePremiumCalculation() {
        var premiumCalls = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> service(false, premiumCalls).createQuote(INPUT));
        assertEquals(0, premiumCalls.get());
    }
}
