package assignments.insurance;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/** Learner-owned creation of an immutable, deterministically timed quote snapshot. */
public final class QuoteVersionService {
    private final String productCode;
    private final String productVersion;
    private final String rateVersion;
    private final String currency;
    private final Duration lifetime;
    private final HomeSecureEligibilityCheck eligibility;
    private final PremiumQuoteCalculator premiumCalculator;
    private final Clock clock;
    private final Supplier<String> quoteIdGenerator;

    public QuoteVersionService(
            String productCode,
            String productVersion,
            String rateVersion,
            String currency,
            Duration lifetime,
            HomeSecureEligibilityCheck eligibility,
            PremiumQuoteCalculator premiumCalculator,
            Clock clock,
            Supplier<String> quoteIdGenerator) {
        this.productCode = productCode;
        this.productVersion = productVersion;
        this.rateVersion = rateVersion;
        this.currency = currency;
        this.lifetime = lifetime;
        this.eligibility = eligibility;
        this.premiumCalculator = premiumCalculator;
        this.clock = clock;
        this.quoteIdGenerator = quoteIdGenerator;
    }

    public QuoteSnapshot createQuote(QuoteCreationInput input) {
        // LEARNER TODO: reject an ineligible decision; calculate once; capture all
        // fixture versions, input and price; use the injected ID and Clock; expiry is
        // creation time plus the supplied fixture lifetime. Never reprice a snapshot.
        throw new UnsupportedOperationException("Day 7 learner TODO: implement quote snapshot creation");
    }

    public boolean isValidAt(QuoteSnapshot quote, Instant now) {
        // LEARNER TODO: a quote is valid strictly before expiresAt, not at equality.
        throw new UnsupportedOperationException("Day 7 learner TODO: implement quote expiry boundary");
    }
}
