package capstone.coverstory;

import capstone.coverstory.application.DemonstratePaidOnboardingAndResume;
import capstone.coverstory.application.ValidateExactApplicationAccessDecisions;
import capstone.coverstory.catalog.InvalidateRetiredCitations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Kept out of setup smoke; executing this profile should stop at the learner-owned TODOs. */
@Tag("learnerAcceptance")
class Day48To56LearnerAcceptanceTest {
    @Test void cap87Resume() {
        var command = new DemonstratePaidOnboardingAndResume();
        assertEquals("RESUMED", command.resume(new DemonstratePaidOnboardingAndResume.Request(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, "mock-event", "retry-1")).status());
    }
    @Test void cap171RetiredCitation() {
        var command = new InvalidateRetiredCitations();
        assertEquals(false, command.evaluate(UUID.randomUUID(), List.of(), List.of()).answerable());
    }
    @Test void cap172ExactAuthorization() {
        var policy = new ValidateExactApplicationAccessDecisions();
        assertEquals(true, policy.authorize(new ValidateExactApplicationAccessDecisions.Request(
                UUID.randomUUID(), ValidateExactApplicationAccessDecisions.Action.READ_JOURNEY,
                UUID.randomUUID(), UUID.randomUUID(), null, false, "ISSUED"), List.of()));
    }
}
