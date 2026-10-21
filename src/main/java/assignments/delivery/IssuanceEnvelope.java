package assignments.delivery;

import java.time.LocalDate;

/** Minimal immutable payload copied from a committed lifecycle event. */
public record IssuanceEnvelope(String policyId, long sourceVersion, String eventType,
                               LocalDate effectiveDate, int schemaVersion) {
    public IssuanceSourceKey sourceKey() {
        return new IssuanceSourceKey(policyId, sourceVersion);
    }

    public void validateSupported() {
        // LEARNER TODO Day 99: validate the bounded ISSUED envelope and its source identity.
        throw new UnsupportedOperationException("Day 99 learner TODO: validate issuance envelope");
    }
}
