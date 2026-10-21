package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** API/UI projection deliberately excludes source identifiers and personal data. */
public record PolicyServicingView(
        String policyId,
        String productVersion,
        BigDecimal annualPremiumRupees,
        String currency,
        PolicyStatus status,
        long version,
        List<PolicyEndorsement> history,
        LocalDate cancellationEffectiveDate) {
    public PolicyServicingView { history = history == null ? List.of() : List.copyOf(history); }
}
