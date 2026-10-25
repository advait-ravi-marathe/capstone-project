package assignments.schema;

import java.util.Set;

/** Independent instructor witness; it is never loaded into learner acceptance. */
final class ReferenceSchemaContractGate {
    static boolean mayContract(Set<String> retiredReaders, Set<String> requiredReaders,
                               Set<String> retiredWriters, Set<String> requiredWriters, boolean backfillComplete) {
        return backfillComplete && retiredReaders != null && requiredReaders != null && !requiredReaders.isEmpty()
                && retiredReaders.containsAll(requiredReaders) && retiredWriters != null && requiredWriters != null
                && !requiredWriters.isEmpty() && retiredWriters.containsAll(requiredWriters);
    }
}
