package assignments.insurance;

/** Fixed evidence inputs for evaluating whether the verified local CI gate is complete. */
public record CiReadinessEvidence(
        boolean java21CompilePasses,
        boolean infrastructurePasses,
        boolean selectedAcceptancePasses,
        boolean cleanCheckoutCommandIsDocumented,
        boolean deliberateFailureReturnsNonzero,
        boolean manifestStatusIsIndependentOfCalendar) { }
