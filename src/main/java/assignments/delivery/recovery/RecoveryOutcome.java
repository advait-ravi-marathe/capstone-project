package assignments.delivery.recovery;

/** Truthful result of a completed local receipt/acknowledgment attempt. */
public enum RecoveryOutcome {
    DELIVERED,
    RECONCILED,
    ALREADY_COMPLETE
}
