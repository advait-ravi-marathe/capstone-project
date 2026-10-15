package capstone.coverstory.application;

import capstone.coverstory.application.internal.ReviewAlertWindow;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP114AcceptanceTest {
    @Test void sixPercentNeedsThreePersistedWindowsAndOneFire() {
        var rule=new ReviewAlertWindow();
        var first=rule.evaluate(ReviewAlertWindow.AlertState.NORMAL,0,
                new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.parse("2027-01-01T00:00:00Z"),100,6));
        assertEquals(ReviewAlertWindow.AlertState.PENDING,first.state());
        assertEquals(1,first.consecutiveQualifyingWindows());
        var second=rule.evaluate(first.state(),first.consecutiveQualifyingWindows(),
                new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.parse("2027-01-01T00:01:00Z"),100,6));
        assertEquals(ReviewAlertWindow.AlertState.PENDING,second.state());
        var third=rule.evaluate(second.state(),second.consecutiveQualifyingWindows(),
                new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.parse("2027-01-01T00:02:00Z"),100,6));
        assertEquals(ReviewAlertWindow.AlertState.FIRING,third.state());
        assertTrue(third.firingTransition());
        var recovered=rule.evaluate(third.state(),third.consecutiveQualifyingWindows(),
                new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.parse("2027-01-01T00:03:00Z"),100,0));
        assertEquals(ReviewAlertWindow.AlertState.RESOLVED,recovered.state());
        assertTrue(recovered.resolutionTransition());
    }

    @Test void exactFivePercentMustNeverQualifyEvenAcrossThreeWindows() {
        var rule=new ReviewAlertWindow();
        var state=ReviewAlertWindow.AlertState.NORMAL; int qualifying=0;
        for(int i=0;i<3;i++) {
            var actual=rule.evaluate(state,qualifying,new ReviewAlertWindow.Window(UUID.randomUUID(),
                    Instant.parse("2027-01-01T00:00:00Z").plusSeconds(i*60L),100,5));
            assertEquals(ReviewAlertWindow.AlertState.NORMAL,actual.state()); state=actual.state(); qualifying=actual.consecutiveQualifyingWindows();
        }
    }

    @Test void lowVolumeDoesNotFireAndNeedsInformationIsNotIssuanceOrRejection() {
        var rule=new ReviewAlertWindow();
        var low=new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.parse("2027-01-01T00:00:00Z"),2,1);
        var actual=rule.evaluate(ReviewAlertWindow.AlertState.NORMAL,0,low);
        assertEquals(ReviewAlertWindow.AlertState.NORMAL,actual.state());
    }

    @Test void TODOSeamIsPresentAndInstructorOracleDoesNotFillIt() {
        var rule=new ReviewAlertWindow();
        assertThrows(UnsupportedOperationException.class,()->rule.evaluate(ReviewAlertWindow.AlertState.NORMAL,0,
                new ReviewAlertWindow.Window(UUID.randomUUID(),Instant.EPOCH,0,0)));
    }
}
