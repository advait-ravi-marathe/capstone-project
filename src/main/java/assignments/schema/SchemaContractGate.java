package assignments.schema;

import java.util.Set;

/** Learner seam for authorizing destructive contract migrations only after old readers retire. */
public final class SchemaContractGate {
    private SchemaContractGate() { }

    public static boolean mayContract(Set<String> activeReaders, Set<String> requiredReaders) {
        // TODO: require a complete, explicit retirement record for every required old reader.
        return false;
    }
}
