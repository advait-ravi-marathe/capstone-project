package assignments.ops;

import java.math.BigDecimal;
import java.util.List;

/** Validates an offline planning record only; it cannot execute AWS actions or claim verification. */
public final class AwsLabDesignPolicy {
    public record Design(String accountId, String region, String accessModel, boolean publicApplication,
                         boolean publicDatabase, boolean wildcardAdmin, boolean hasSecrets,
                         boolean cloudActionRequested, boolean networkingSelected, List<CostLine> costs,
                         BigDecimal total, String currency, boolean cloudVerified) {}
    public record CostLine(String component, BigDecimal fictionalRate, String unit,
                           BigDecimal quantity, BigDecimal amount, String currency, String fixtureLabel) {}

    private static final List<String> REQUIRED_COMPONENTS = List.of(
            "compute", "database-storage", "backups-snapshots", "logs", "secrets", "transfer");

    public void validate(Design design) {
        // LEARNER TODO Day 137/138: validate bounded offline design and explicit fictional-cost inputs.
        throw new UnsupportedOperationException("Day 137/138 learner TODO: validate offline AWS design");
    }
}
