package assignments.reboot;

import assignments.insurance.Day28ReadinessReport;
import assignments.insurance.Milestone2Readiness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** This contract verifies blocked readiness; it does not imitate the real E2E gate. */
@Tag("learnerAcceptance")
class Day28ContractTest {
    @Test
    void incompletePrerequisitesReportBlockedWithExactDays() {
        Map<Integer, String> statuses = IntStream.rangeClosed(14, 27).boxed()
                .collect(Collectors.toMap(day -> day, day -> "not_attempted"));
        Day28ReadinessReport report = new Milestone2Readiness().evaluate(statuses);
        assertEquals("BLOCKED", report.status());
        assertEquals(IntStream.rangeClosed(14, 27).boxed().toList(), report.missingPrerequisiteDays());
    }
}
