package assignments.ops;

import java.util.List;

/** Learner seam for fixed safe pg_restore options; process setup remains instructor-owned. */
public final class RestoreCommandPolicy {
    public List<String> arguments(String sourceDatabase, String targetDatabase,
                                  String archiveSha256, String expectedArchiveSha256,
                                  boolean targetEmpty, List<String> requestedOptions) {
        // LEARNER TODO Day 131: require a run-owned empty target and fixed non-destructive options.
        throw new UnsupportedOperationException("Day 131 learner TODO: validate safe restore request");
    }
}
