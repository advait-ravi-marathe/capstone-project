package assignments.ops;

import java.math.BigDecimal;
import java.util.List;

/** Validates an offline planning record only; it cannot execute AWS actions or claim verification. */
public final class AwsLabDesignPolicy {
    public record Design(String accountId, String region, String accessModel, boolean publicApplication,
                         boolean wildcardAdmin, boolean hasSecrets, List<CostLine> costs,
                         BigDecimal total, String currency, boolean cloudVerified) {}
    public record CostLine(String component, BigDecimal fictionalRate, BigDecimal hours,
                           BigDecimal amount, String fixtureLabel) {}

    public void validate(Design design) {
        // LEARNER TODO Day 137/138: validate bounded offline design and explicit fictional-cost inputs.
        throw new UnsupportedOperationException("Day 137/138 learner TODO: validate offline AWS design");
    }
}
