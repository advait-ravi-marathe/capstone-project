package assignments.reboot;

import assignments.insurance.DatabaseReadinessIndicator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Learner policy tests; database readiness logic is intentionally unimplemented. */
@Tag("learnerAcceptance")
class Day23ContractTest {
    @Test
    void reportsUpWithoutDetailsForAvailableDatabase() {
        var result = new DatabaseReadinessIndicator(() -> true).health();
        assertEquals("UP", result.getStatus().getCode());
        assertEquals(0, result.getDetails().size());
    }

    @Test
    void reportsDownWithoutDetailsForUnavailableDatabase() {
        var result = new DatabaseReadinessIndicator(() -> false).health();
        assertEquals("DOWN", result.getStatus().getCode());
        assertEquals(0, result.getDetails().size());
    }
}
