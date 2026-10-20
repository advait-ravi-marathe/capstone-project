package assignments.delivery.recovery;

public final class RecoveryPreconditionException extends RuntimeException {
    public enum Code { NOT_FOUND, STALE_REVISION, ALREADY_HELD, NOT_HELD, RECEIPT_PRESENT, RECEIPT_CONFLICT,
        ACKNOWLEDGED, ACK_WITHOUT_RECEIPT, QUARANTINED, UNSUPPORTED_ENVELOPE, AMBIENT_TRANSACTION, INVALID_REASON }

    private final Code code;
    public RecoveryPreconditionException(Code code) {
        super("manual recovery precondition failed: " + code.name());
        this.code = code;
    }
    public Code code() { return code; }
}
