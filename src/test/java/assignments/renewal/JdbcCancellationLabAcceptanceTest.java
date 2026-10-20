package assignments.renewal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class JdbcCancellationLabAcceptanceTest {
    @Test
    void futureTimeoutDoesNotCancelSqlAndWorkerOwnsCleanup() throws Exception {
        try (JdbcCancellationFixture fixture = JdbcCancellationFixture.start()) {
            CancellationEvidence evidence = runCancellationLab(fixture);
            assertTrue(evidence.futureWaitTimedOut());
            assertTrue(evidence.serverWorkStillActiveAfterFutureTimeout());
            assertTrue(evidence.cancelledTheObservedStatement());
            assertEquals("57014", evidence.workerSqlState());
            assertTrue(evidence.workerRolledBack());
            assertTrue(evidence.workerClosed());
            assertTrue(evidence.futureCompletedAfterCancel());
            assertEquals(fixture.backendPid(), evidence.reborrow().pid(), "one-slot pool proves same backend reuse");
            assertEquals(fixture.baselineLock(), evidence.reborrow().lockTimeout());
            assertEquals(fixture.baselineStatement(), evidence.reborrow().statementTimeout());
        }
    }

    private CancellationEvidence runCancellationLab(JdbcCancellationFixture fixture) throws Exception {
        // LEARNER TODO Day 95: show Future.get(timeout) only stops waiting, observe PgSleep and
        // the exact active Statement, call cancel() on that Statement, await worker completion,
        // preserve its SQLState, then prove worker rollback/close before same-backend reborrow.
        throw new UnsupportedOperationException("Day 95 learner TODO: coordinate JDBC cancellation lab");
    }

    private record CancellationEvidence(boolean futureWaitTimedOut, boolean serverWorkStillActiveAfterFutureTimeout,
                                        boolean cancelledTheObservedStatement, String workerSqlState,
                                        boolean workerRolledBack, boolean workerClosed,
                                        boolean futureCompletedAfterCancel,
                                        JdbcCancellationFixture.Reborrow reborrow) { }
}
