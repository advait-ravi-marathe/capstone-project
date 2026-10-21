package assignments.insurance;

/** Stable domain conflict for reusing an idempotency key with different intent. */
public final class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) { super(message); }
}
