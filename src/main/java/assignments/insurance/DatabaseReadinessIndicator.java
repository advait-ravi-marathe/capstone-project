package assignments.insurance;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/** Learner-owned health contributor; public responses must not reveal connection details. */
public final class DatabaseReadinessIndicator implements HealthIndicator {
    private final DatabaseReadinessProbe probe;

    public DatabaseReadinessIndicator(DatabaseReadinessProbe probe) {
        this.probe = probe;
    }

    @Override
    public Health health() {
        // LEARNER TODO: report UP when the probe succeeds and DOWN otherwise.
        // Never include host, credentials, SQL exception text, or environment data.
        throw new UnsupportedOperationException("Day 23 learner TODO: implement database readiness health");
    }
}
