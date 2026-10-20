package assignments.reboot;

import assignments.insurance.CiReadinessEvidence;
import assignments.insurance.CiReadinessPolicy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Day 13 checks readiness reporting; it does not claim that a remote CI run occurred. */
@Tag("learnerAcceptance")
class Day13ContractTest {
    private final CiReadinessPolicy policy = new CiReadinessPolicy();

    @Test
    void reportsEveryMissingSignalWithoutCalendarGreenwashing() {
        var report = policy.assess(new CiReadinessEvidence(
                true, false, false, true, false, true));
        assertFalse(report.ready());
        assertEquals(java.util.List.of(
                "infrastructure_tests", "selected_acceptance_tests", "failure_propagation"),
                report.blockers());
    }

    @Test
    void allObservedSignalsAreRequiredForReadyState() {
        var report = policy.assess(new CiReadinessEvidence(true, true, true, true, true, true));
        assertTrue(report.ready());
        assertEquals(java.util.List.of(), report.blockers());
    }

    @Test
    void missingCleanCheckoutAndCalendarIndependentStatusStayBlocked() {
        var report = policy.assess(new CiReadinessEvidence(true, true, true, false, true, false));
        assertFalse(report.ready());
        assertEquals(java.util.List.of("clean_checkout_command", "manifest_calendar_independence"),
                report.blockers());
    }
}
