package assignments.ops;

import java.util.List;

/** Learner policy for a fixed full-database pg_dump command over instructor-owned fixture handles. */
public final class BackupCommandPolicy {
    public List<String> arguments(String sourceDatabase, String format, List<String> requestedOptions) {
        // LEARNER TODO Day 130: choose only the fixed custom-archive options and reject destructive scope.
        throw new UnsupportedOperationException("Day 130 learner TODO: validate bounded backup options");
    }
}
