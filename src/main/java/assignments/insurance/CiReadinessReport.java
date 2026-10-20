package assignments.insurance;

import java.util.List;

/** Result of an evidence-based CI readiness check. */
public record CiReadinessReport(boolean ready, List<String> blockers) {
    public CiReadinessReport {
        blockers = List.copyOf(blockers);
    }
}
