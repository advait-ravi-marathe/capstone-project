package assignments.insurance;

/** Audit sink accepts only the selected request ID, never request data or credentials. */
@FunctionalInterface
public interface RequestAudit {
    void recordClientError(String requestId);
}
