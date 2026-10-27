package assignments.local;

/** Safe failure type; callers must not expose provider/network exception text. */
public final class AiAssistantUnavailableException extends RuntimeException {
    public AiAssistantUnavailableException() { super("AI training service unavailable"); }
}
