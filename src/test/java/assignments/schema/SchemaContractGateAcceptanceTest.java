package assignments.schema;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class SchemaContractGateAcceptanceTest {
    @Test void readers_writers_and_complete_backfill_are_all_required_before_contract() {
        Set<String> readers = Set.of("legacy-policy-reader", "legacy-reporting-job");
        Set<String> writers = Set.of("legacy-policy-writer");
        assertFalse(SchemaContractGate.mayContract(readers, readers, writers, writers, false));
        assertFalse(SchemaContractGate.mayContract(Set.of("legacy-policy-reader"), readers, writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, readers, Set.of(), writers, true));
        assertFalse(SchemaContractGate.mayContract(null, readers, writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, Set.of(), writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, readers, writers, Set.of(), true));
        assertTrue(SchemaContractGate.mayContract(readers, readers, writers, writers, true));
    }
}
