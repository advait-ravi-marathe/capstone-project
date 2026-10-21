package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Instructor-owned construction plumbing for synthetic candidates; it does not decide receipts or eligibility. */
@FunctionalInterface
public interface PolicyIssueSnapshotFactory {
    PolicySnapshot create(PolicyIssueRequest request, String authenticatedOwnerId);

    /** Fixed local coursework facts only; no catalog, receipt, SQL, validation, or event behavior. */
    static PolicyIssueSnapshotFactory trainingFixtures() {
        AtomicInteger sequence = new AtomicInteger();
        return (request, owner) -> {
            if (request == null || owner == null || owner.isBlank()) throw new IllegalArgumentException("unknown training fixture");
            if (!"journey-a".equals(request.journeyId()) || !"quote-a".equals(request.quoteVersionId()))
                throw new IllegalArgumentException("unknown training quote fixture");
            String id = sequence.getAndIncrement() == 0 ? "policy-a" : "policy-training-" + sequence.get();
            return new PolicySnapshot(id, owner, request.journeyId(), request.quoteVersionId(), "home-secure-v1",
                    "training-rate-v1", new BigDecimal("125.00"), "INR", request.effectiveDate(),
                    PolicyStatus.ISSUED, 1, java.util.List.of(), null);
        };
    }
}
