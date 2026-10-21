package assignments.ops;

/** Learner policy for fixture handles owned by one local rehearsal invocation. */
public final class RehearsalTargetPolicy {
    public record FixtureHandle(String id, String host, String database, boolean issuedByThisRun) {}

    public void validate(FixtureHandle source, FixtureHandle target, boolean cloudExecutionRequested) {
        // LEARNER TODO Day 127: accept only distinct run-owned literal-loopback fixtures.
        throw new UnsupportedOperationException("Day 127 learner TODO: validate rehearsal fixture ownership");
    }
}
