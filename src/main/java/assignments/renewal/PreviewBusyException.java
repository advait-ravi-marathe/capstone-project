package assignments.renewal;

/** Safe domain signal that process-local read capacity was already occupied. */
public final class PreviewBusyException extends RuntimeException {
    public PreviewBusyException() { super("Renewal preview capacity is busy"); }
}
