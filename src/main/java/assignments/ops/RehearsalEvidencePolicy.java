package assignments.ops;

/** Learner decisions over instructor-produced snapshots and phase evidence. */
public final class RehearsalEvidencePolicy {
    public record PhaseEvidence(boolean archiveCreated, boolean restoreVerified,
                                boolean applicationChecked, boolean cleanupComplete,
                                String sourceSnapshot, String restoredSnapshot) {}

    public void requireRestoreMatch(String sourceSnapshot, String restoredSnapshot) {
        // LEARNER TODO Day 129: compare ordered identities, payloads/timestamps, schema and migrations.
        throw new UnsupportedOperationException("Day 129 learner TODO: compare meaningful database evidence");
    }

    public void requireActivationEvidence(PhaseEvidence evidence) {
        // LEARNER TODO Day 132: health alone cannot substitute for restore and business evidence.
        throw new UnsupportedOperationException("Day 132 learner TODO: require restore and application evidence");
    }

    public void requireSuccessfulCleanup(PhaseEvidence evidence, boolean unrelatedSentinelAlive) {
        // LEARNER TODO Day 136: fail the rehearsal when an owned child/artifact is not cleaned up.
        throw new UnsupportedOperationException("Day 136 learner TODO: report owned cleanup results");
    }

    public void requireOfflineReleaseGate(boolean archiveRestored, boolean fullStateMatched,
                                          boolean packagedAppChecked, boolean sourceUnchanged,
                                          boolean loopbackOnly, boolean cloudActionAttempted) {
        // LEARNER TODO Day 139: report local evidence without converting it into AWS readiness.
        throw new UnsupportedOperationException("Day 139 learner TODO: distinguish local proof from cloud readiness");
    }
}
