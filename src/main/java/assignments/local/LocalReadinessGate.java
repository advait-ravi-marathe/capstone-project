package assignments.local;

import java.util.Map;

/** Day 57 seam: a missing or failed prerequisite replaces new work with recovery. */
public final class LocalReadinessGate {
    public Readiness assess(Map<String, Boolean> requiredEvidence) {
        // LEARNER TODO Day 57: fail closed for missing, empty, or failed evidence.
        throw new UnsupportedOperationException("Day 57 learner TODO: assess prior evidence");
    }

    public SecurityCheckpoint securityCheckpoint(Map<String, Boolean> evidence, int implementationMinutes) {
        // LEARNER TODO Day 63: use recovery inside today's 85-minute engineering block, with no added overtime.
        throw new UnsupportedOperationException("Day 63 learner TODO: record bounded security checkpoint");
    }

    public enum Readiness { READY, RECOVERY_REQUIRED }

    public record SecurityCheckpoint(boolean recoveryRequired, int engineeringMinutes, boolean newWorkAdded) { }
}
