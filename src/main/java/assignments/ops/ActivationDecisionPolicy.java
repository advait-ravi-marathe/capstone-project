package assignments.ops;

/** A local rehearsal changes only its test-client target after the candidate gates pass. */
public final class ActivationDecisionPolicy {
    public enum Selection { CURRENT, CANDIDATE }

    public Selection choose(boolean distinctTarget, boolean artifactVerified,
                            boolean applicationReady, boolean businessDataVerified,
                            boolean loopbackOnly) {
        // LEARNER TODO Day 134: retain CURRENT unless every candidate gate is proven.
        throw new UnsupportedOperationException("Day 134 learner TODO: guard local activation");
    }
}
