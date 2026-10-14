package coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("learnerAcceptance")
class CAP60AcceptanceTest {
    @Test void concurrentAdvanceSameKey() {
        fail("TODO(CAP-60): write the bounded two-connection same-key race and assert one effect, audit event, and journey advance");
    }

    @Test void differentCommandsAtVersionFourHaveOneWinnerAndOneConflict() {
        fail("TODO(CAP-60): prove optimistic version conflict without overwrite");
    }

    @Test void everyInjectedWriteFailureRollsBackAllBusinessAndJourneyRows() {
        fail("TODO(CAP-60): exercise rollback after each supplied write boundary");
    }
}
