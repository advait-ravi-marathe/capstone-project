package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Immutable synthetic policy snapshot and servicing history. */
public record PolicySnapshot(
        String policyId,
        String ownerId,
        String journeyId,
        String quoteVersionId,
        String productVersion,
        String rateVersion,
        BigDecimal annualPremiumRupees,
        String currency,
        LocalDate effectiveDate,
        PolicyStatus status,
        long version,
        List<PolicyEndorsement> endorsements,
        LocalDate cancellationEffectiveDate) {
    public PolicySnapshot {
        endorsements = endorsements == null ? List.of() : List.copyOf(endorsements);
    }
}
