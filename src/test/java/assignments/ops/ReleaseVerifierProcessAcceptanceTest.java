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
        Process sentinel = new ProcessBuilder("sleep", "30").start();
        try {
            var timeout = runner.run(List.of("/bin/sh", "-c", "sleep 30 & echo $!; wait"), Duration.ofMillis(200), 4096);
            assertTrue(timeout.timedOut());
            assertFalse(timeout.successful());
            assertFalse(processRunning(timeout.pid()), "owned timeout child is reaped");
            long descendantPid=Long.parseLong(timeout.capturedOutput().trim());
            assertFalse(processRunning(descendantPid),"owned child process tree is terminated");

            var overflow = runner.run(List.of("/bin/sh", "-c", "yes COVERSTORY_INSPECTION_REPORT"), Duration.ofSeconds(5), 1024);
            assertTrue(overflow.outputOverflow());
            assertFalse(overflow.successful());
            assertFalse(processRunning(overflow.pid()), "owned overflow child is terminated");

            var plausibleThenFailure = runner.run(List.of("/bin/sh", "-c", "printf 'COVERSTORY_INSPECTION_REPORT {}\\n'; exit 7"), Duration.ofSeconds(2), 4096);
            assertFalse(plausibleThenFailure.successful());
            assertEquals(7, plausibleThenFailure.exitCode());
            assertFalse(processRunning(plausibleThenFailure.pid()));
            assertTrue(sentinel.isAlive(), "unrelated sentinel process must remain alive");
        } finally {
            sentinel.destroyForcibly();
            assertTrue(sentinel.waitFor(5, java.util.concurrent.TimeUnit.SECONDS), "sentinel cleanup is explicit");
        }
    }

    @Test void day152ReapsBackgroundDescendantsAfterNormalAndNonzeroRootExit() throws Exception {
        Process sentinel = new ProcessBuilder("sleep", "30").start();
        try {
            var success = runner.run(List.of("/bin/sh", "-c", "sleep 30 >/dev/null 2>&1 & echo $!; exit 0"), Duration.ofSeconds(3), 4096);
            assertTrue(success.successful(), success.capturedOutput());
            long successfulDescendant = Long.parseLong(success.capturedOutput().trim());
            assertFalse(processRunning(successfulDescendant),
                    "a background child is owned and reaped even after its root exits successfully");

            var failure = runner.run(List.of("/bin/sh", "-c", "sleep 30 >/dev/null 2>&1 & echo $!; exit 7"), Duration.ofSeconds(3), 4096);
            assertEquals(7, failure.exitCode());
            long failedRootDescendant = Long.parseLong(failure.capturedOutput().trim());
            assertFalse(processRunning(failedRootDescendant),
                    "a background child is reaped after nonzero root exit");
            assertTrue(sentinel.isAlive(), "unrelated sentinel process remains alive");
        } finally {
            sentinel.destroyForcibly();
            assertTrue(sentinel.waitFor(5, java.util.concurrent.TimeUnit.SECONDS));
        }
    }

    @Test void day152ForciblyReapsTermResistantOwnedDescendants() throws Exception {
        Process sentinel = new ProcessBuilder("sleep", "30").start();
        try {
            var result = runner.run(List.of("/bin/sh", "-c",
                    "(trap '' TERM; exec sleep 30) & child=$!; echo $child; wait"),
                    Duration.ofMillis(250), 4096);
            assertTrue(result.timedOut());
            long ownedPid = Long.parseLong(result.capturedOutput().trim());
            assertFalse(processRunning(ownedPid), "TERM-resistant owned child is forcibly terminated and awaited");
            assertFalse(processRunning(result.pid()), "timed-out root is reaped after descendant cleanup");
            assertTrue(sentinel.isAlive(), "unrelated sentinel remains alive");
        } finally {
            sentinel.destroyForcibly();
            assertTrue(sentinel.waitFor(5, java.util.concurrent.TimeUnit.SECONDS));
        }
    }

    private static boolean processRunning(long pid) throws Exception {
        var stat = java.nio.file.Path.of("/proc", Long.toString(pid), "stat");
        if (!java.nio.file.Files.exists(stat)) return false;
        String value = java.nio.file.Files.readString(stat);
        String[] fields = value.substring(value.lastIndexOf(')') + 1).trim().split("\\s+");
        return !fields[0].equals("Z") && !fields[0].equals("X");
    }
}
