package assignments.renewal;

/** Safe domain exception for the narrow PostgreSQL budget/cancel SQL states. */
public final class PreviewReadInterruptedException extends RuntimeException {
    public PreviewReadInterruptedException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
