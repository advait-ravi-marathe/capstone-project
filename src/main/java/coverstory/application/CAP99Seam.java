package coverstory.application;

import java.util.List;

/** CAP-99 local evidence manifest seam. It never authorizes deployment or release. */
public final class CAP99Seam {
    public enum Gate { ARTIFACT, REDACTED_CONFIG, MIGRATION_COMPATIBILITY_RESTORE,
        OWNERSHIP, PAYMENT_RECOVERY, GROUNDED_ASSISTANCE }
    public enum EvidenceStatus { VERIFIED, FAIL, UNVERIFIED }
    public enum EvidenceKind { TEST_OUTPUT, PLANNED_PATH, SCREENSHOT, NONE }
    public enum Decision { READY_FOR_REVIEW, BLOCKED, UNVERIFIED }

    public record Receipt(EvidenceKind kind, String command, String observedOutput,
                          String sourceCommit, String fixtureSha256) { }
    public record GateEvidence(Gate gate, EvidenceStatus status, Receipt receipt,
                               String issue) { }
    public record ReadinessDecision(Decision decision, List<String> findings) {
        public ReadinessDecision { findings = List.copyOf(findings); }
    }

    public ReadinessDecision evaluateReadiness(List<GateEvidence> sixGates) {
        // LEARNER TODO: validate exact gate coverage and evidence receipts, retain
        // cross-owner failures as hard blockers, and report missing prerequisites.
        throw new UnsupportedOperationException("CAP-99 learner TODO: validate readiness manifest");
    }
}
