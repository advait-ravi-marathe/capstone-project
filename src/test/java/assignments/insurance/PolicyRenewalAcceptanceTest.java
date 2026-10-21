package assignments.insurance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/** Fixed Day 45–47 domain acceptance. Red only at the named learner policy seams. */
@Tag("learnerAcceptance")
class PolicyRenewalAcceptanceTest {
    private static final PolicyRenewalPreviewPolicy POLICY = new PolicyRenewalPreviewPolicy();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2027-11-15T12:00:00Z"), ZoneOffset.UTC);

    @Test void day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow() {
        var ordinary = POLICY.termFor(LocalDate.parse("2026-12-01"));
        assertEquals(LocalDate.parse("2026-12-01"), ordinary.currentTermStart());
        assertEquals(LocalDate.parse("2027-12-01"), ordinary.nextTermStart());
        assertEquals(LocalDate.parse("2027-11-01"), ordinary.renewalWindowOpensOn());

        var leapDay = POLICY.termFor(LocalDate.parse("2024-02-29"));
        assertEquals(LocalDate.parse("2025-02-28"), leapDay.nextTermStart());
        assertEquals(LocalDate.parse("2025-01-29"), leapDay.renewalWindowOpensOn());
    }

    @Test void day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries() {
        var facts = issuedFacts(LocalDate.parse("2026-12-01"));
        assertEquals(PolicyRenewalEligibility.TOO_EARLY,
                POLICY.eligibility(facts, LocalDate.parse("2027-10-31")));
        assertEquals(PolicyRenewalEligibility.AVAILABLE,
                POLICY.eligibility(facts, LocalDate.parse("2027-11-01")));
        assertEquals(PolicyRenewalEligibility.AVAILABLE,
                POLICY.eligibility(facts, LocalDate.parse("2027-11-30")));
        assertEquals(PolicyRenewalEligibility.TERM_ENDED,
                POLICY.eligibility(facts, LocalDate.parse("2027-12-01")));
        assertEquals(PolicyRenewalEligibility.NOT_RENEWABLE,
                POLICY.eligibility(withStatus(facts, PolicyStatus.CANCELLED), LocalDate.parse("2027-11-15")));
        assertEquals(PolicyRenewalEligibility.NOT_RENEWABLE,
                POLICY.eligibility(withStatus(facts, PolicyStatus.CANCEL_SCHEDULED), LocalDate.parse("2027-11-15")));
    }

    @Test void day47PreviewCopiesCurrentSnapshotAndMakesNoRenewalPriceOrCommitment() {
        var preview = POLICY.buildPreview(issuedFacts(LocalDate.parse("2026-12-01")));
        assertAll("non-binding reference snapshot",
                () -> assertEquals("policy-demo-1", preview.policyId()),
                () -> assertEquals(3L, preview.sourcePolicyVersion()),
                () -> assertEquals("home-secure-v1", preview.productVersion()),
                () -> assertEquals("home-secure-rate-v1", preview.rateVersion()),
                () -> assertEquals(new BigDecimal("125.00"), preview.currentAnnualPremiumRupees()),
                () -> assertEquals("INR", preview.currency()),
                () -> assertEquals(LocalDate.parse("2027-12-01"), preview.nextTermEffectiveDate()),
                () -> assertTrue(preview.notice().toLowerCase().contains("not a renewal offer")));
    }

    private static PolicyRenewalFacts issuedFacts(LocalDate effectiveDate) {
        return new PolicyRenewalFacts("policy-demo-1", effectiveDate, PolicyStatus.ISSUED, 3,
                "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR");
    }

    private static PolicyRenewalFacts withStatus(PolicyRenewalFacts facts, PolicyStatus status) {
        return new PolicyRenewalFacts(facts.policyId(), facts.effectiveDate(), status, facts.policyVersion(),
                facts.productVersion(), facts.rateVersion(), facts.currentAnnualPremiumRupees(), facts.currency());
    }
}
