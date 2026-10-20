package assignments.insurance;

/** Read-only connectivity probe used only by the readiness health contributor. */
@FunctionalInterface
public interface DatabaseReadinessProbe {
    boolean isAvailable();
}
