package assignments.ops;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ReleaseVerifierProcessAcceptanceTest {
    private final BoundedProcessRunner runner = new BoundedProcessRunner();

    @Test void day152RejectsTimeoutOverflowAndPlausibleNonzeroOutput() throws Exception {
        Process sentinel = new ProcessBuilder("/bin/sh", "-c", "sleep 30").start();
        try {
            var timeout = runner.run(List.of("/bin/sh", "-c", "sleep 30 & echo $!; wait"), Duration.ofMillis(200), 4096);
            assertTrue(timeout.timedOut());
            assertFalse(timeout.successful());
            assertFalse(ProcessHandle.of(timeout.pid()).map(ProcessHandle::isAlive).orElse(false), "owned timeout child is reaped");
            long descendantPid=Long.parseLong(timeout.capturedOutput().trim());
            assertFalse(ProcessHandle.of(descendantPid).map(ProcessHandle::isAlive).orElse(false),"owned child process tree is reaped");

            var overflow = runner.run(List.of("/bin/sh", "-c", "yes COVERSTORY_INSPECTION_REPORT"), Duration.ofSeconds(5), 1024);
            assertTrue(overflow.outputOverflow());
            assertFalse(overflow.successful());
            assertFalse(ProcessHandle.of(overflow.pid()).map(ProcessHandle::isAlive).orElse(false), "owned overflow child is reaped");

            var plausibleThenFailure = runner.run(List.of("/bin/sh", "-c", "printf 'COVERSTORY_INSPECTION_REPORT {}\\n'; exit 7"), Duration.ofSeconds(2), 4096);
            assertFalse(plausibleThenFailure.successful());
            assertEquals(7, plausibleThenFailure.exitCode());
            assertFalse(ProcessHandle.of(plausibleThenFailure.pid()).map(ProcessHandle::isAlive).orElse(false));
            assertTrue(sentinel.isAlive(), "unrelated sentinel process must remain alive");
        } finally {
            sentinel.destroyForcibly();
            assertTrue(sentinel.waitFor(5, java.util.concurrent.TimeUnit.SECONDS), "sentinel cleanup is explicit");
        }
    }
}
