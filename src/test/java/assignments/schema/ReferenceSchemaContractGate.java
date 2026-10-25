package assignments.schema;

import java.util.Set;

/** Independent instructor witness; it is never loaded into learner acceptance. */
final class ReferenceSchemaContractGate {
    static boolean mayContract(Set<String> activeReaders, Set<String> requiredReaders) {
        return activeReaders != null && requiredReaders != null && !requiredReaders.isEmpty()
                && activeReaders.containsAll(requiredReaders);
    }
}
