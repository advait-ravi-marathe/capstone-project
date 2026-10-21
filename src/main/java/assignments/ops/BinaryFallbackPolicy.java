package assignments.ops;

/** Requires evidence for this exact pair and fixture; labels and booleans alone are insufficient. */
public final class BinaryFallbackPolicy {
    public FallbackDecision decide(ReleaseArtifactPair evidence, String currentBaselineSha,
                                   String currentCandidateSha, String currentFixture,
                                   String currentSchemaFingerprint) {
        // LEARNER TODO Day 150: bind fallback to actual distinct artifacts, same fixture/schema,
        // successful legacy/report matrix, retained state and cleanup. Missing evidence fails closed.
        throw new UnsupportedOperationException("Day 150 learner TODO: require bound runtime evidence before fallback");
    }

    public enum FallbackDecision { ALLOW_TESTED_PAIR, FORWARD_REPAIR_REQUIRED, BLOCKED }
}
