package assignments.renewal;

/** Safe domain signal that the database connection pool could not provide a connection. */
public final class PreviewUnavailableException extends RuntimeException {
    public PreviewUnavailableException(Throwable cause) { super("Renewal preview is temporarily unavailable", cause); }
}
