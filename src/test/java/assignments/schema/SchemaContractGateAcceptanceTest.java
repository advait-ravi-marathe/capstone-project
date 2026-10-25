package assignments.schema;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class SchemaContractGateAcceptanceTest {
    @Test void every_required_old_reader_must_be_retired_before_contract() {
        Set<String> required = Set.of("legacy-policy-reader", "legacy-reporting-job");
        assertFalse(SchemaContractGate.mayContract(Set.of("legacy-policy-reader"), required));
        assertTrue(SchemaContractGate.mayContract(required, required));
        assertFalse(SchemaContractGate.mayContract(null, required));
        assertFalse(SchemaContractGate.mayContract(required, null));
    }
}
