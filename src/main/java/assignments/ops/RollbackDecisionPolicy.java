package assignments.ops;

/** Same-artifact, quiesced configuration rollback; not a schema downgrade or binary rollback. */
public final class RollbackDecisionPolicy {
    public boolean mayReturnToSource(boolean knownGoodArtifactPresent, boolean schemaCompatible,
                                     boolean noAcceptedWritesAfterDivergence) {
        // LEARNER TODO Day 135: refuse blind rollback to stale source state after candidate writes.
        throw new UnsupportedOperationException("Day 135 learner TODO: block unsafe configuration rollback");
    }
}
