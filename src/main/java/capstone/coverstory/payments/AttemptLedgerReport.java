package capstone.coverstory.payments;

/** Result vocabulary for the bounded local ledger validator. */
public record AttemptLedgerReport(Status status, int acceptedRows) {
    public enum Status { VALID, DUPLICATE_ATTEMPT_ID, NON_MONOTONIC, INVALID_START, INVALID_DOMAIN }
}
