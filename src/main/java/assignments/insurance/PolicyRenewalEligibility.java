package assignments.insurance;

/** Read-only renewal preview decision; it does not bind or extend a policy. */
public enum PolicyRenewalEligibility {
    AVAILABLE,
    TOO_EARLY,
    TERM_ENDED,
    NOT_RENEWABLE
}
