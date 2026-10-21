package assignments.delivery.recovery;

public enum RecoveryReason {
    OBSERVED_FAILURE, RECEIPT_CONFLICT, MANUAL_HOLD, REVIEWED_FOR_RETRY

    ;
    public boolean isQuarantineReason() {
        return this == OBSERVED_FAILURE || this == RECEIPT_CONFLICT || this == MANUAL_HOLD;
    }
}
