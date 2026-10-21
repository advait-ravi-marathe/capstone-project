package assignments.delivery.recovery;

/** Projects the existing exact-key observation/classifier result; it performs no recovery work. */
public final class InspectionReportMapper {
    private final RecoveryDecisionPolicy decisions;
    public InspectionReportMapper(RecoveryDecisionPolicy decisions) { this.decisions = decisions; }

    public InspectionReport map(RecoveryReadResult result) {
        // LEARNER TODO Day 142: preserve typed observed fields and nulls, classify the existing
        // observation, cap supplementary history at 20, and emit only the allowlisted version-1 fields.
        throw new UnsupportedOperationException("Day 142 learner TODO: project existing inspection evidence");
    }
}
