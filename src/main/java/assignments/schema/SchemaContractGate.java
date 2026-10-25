package assignments.schema;

import java.util.Set;

/** Learner seam for destructive cleanup after both old access paths retire and backfill is complete. */
public final class SchemaContractGate {
    private SchemaContractGate() { }

    public static boolean mayContract(Set<String> retiredReaders, Set<String> requiredReaders,
                                      Set<String> retiredWriters, Set<String> requiredWriters,
                                      boolean backfillComplete) {
        // TODO: require reader and writer retirement records and a verified complete backfill.
        return false;
    }
}
