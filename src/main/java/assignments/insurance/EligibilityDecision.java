package assignments.insurance;

/** Stable result shape for a pure synthetic eligibility decision. */
public record EligibilityDecision(boolean eligible, String reasonCode) { }
