package assignments.delivery.recovery;

import java.util.List;

/** Validates the opt-in report mode before a Spring context or datasource is constructed. */
public final class ReleaseInspectionRequestPolicy {
    public ReleaseInspectionRequest parse(String[] args) {
        // LEARNER TODO Day 145: accept one explicit release.inspect mode and exact key fields;
        // reject duplicate/unknown/mutating options before any database or store access.
        throw new UnsupportedOperationException("Day 145 learner TODO: validate inspection command request");
    }
}
