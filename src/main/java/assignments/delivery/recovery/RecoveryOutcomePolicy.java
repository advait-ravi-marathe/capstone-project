package assignments.delivery.recovery;

/** Maps a successful one-key retry to a truthful local outcome. */
public final class RecoveryOutcomePolicy {
    public RecoveryOutcome afterAcknowledgment(RecoveryObservation beforeAttempt) {
        // LEARNER TODO Day 121: distinguish a newly created local receipt from reconciliation
        // of an already matching receipt; do not report unknown failures as successful outcomes.
        throw new UnsupportedOperationException("Day 121 learner TODO: map observed recovery outcome");
    }
}
