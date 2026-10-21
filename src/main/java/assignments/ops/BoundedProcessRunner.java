package assignments.ops;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicBoolean;

/** Instructor-supplied bounded child-process plumbing. The runner owns the complete process tree. */
public final class BoundedProcessRunner {
    public Result run(List<String> command, Duration timeout, int maxOutputBytes) throws IOException, InterruptedException {
        return run(command, Map.of(), timeout, maxOutputBytes);
    }

    public Result run(List<String> command, Map<String, String> environment, Duration timeout, int maxOutputBytes)
            throws IOException, InterruptedException {
        if (command == null || command.isEmpty() || timeout.isZero() || timeout.isNegative() || maxOutputBytes < 1)
            throw new IllegalArgumentException("invalid bounded process request");
        Path setsid = Path.of("/usr/bin/setsid");
        if (!Files.isExecutable(setsid)) throw new IOException("bounded process tree ownership requires /usr/bin/setsid");
        var isolatedCommand = new java.util.ArrayList<String>();
        isolatedCommand.add(setsid.toString());
        isolatedCommand.add("--wait");
        isolatedCommand.addAll(command);
        ProcessBuilder builder = new ProcessBuilder(isolatedCommand).redirectErrorStream(true);
        builder.environment().putAll(environment);
        Process child = builder.start();
        ProcessHandle root = child.toHandle();
        long pid = child.pid();
        ConcurrentHashMap<Long, ProcessHandle> owned = new ConcurrentHashMap<>();
        AtomicBoolean monitoring = new AtomicBoolean(true);
        Thread monitor = Thread.ofPlatform().daemon().name("release-process-tree-monitor").start(() -> {
            while (monitoring.get()) {
                rememberDescendants(root, owned);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2));
            }
            rememberDescendants(root, owned);
        });
        ByteArrayOutputStream retained = new ByteArrayOutputStream(Math.min(maxOutputBytes, 8192));
        AtomicBoolean overflow = new AtomicBoolean();
        Thread reader = Thread.ofPlatform().daemon().name("release-output-reader").start(() -> {
            try (var input = child.getInputStream()) {
                byte[] chunk = new byte[4096];
                int count;
                while ((count = input.read(chunk)) >= 0) {
                    synchronized (retained) {
                        int remaining = maxOutputBytes - retained.size();
                        if (count > remaining) {
                            if (remaining > 0) retained.write(chunk, 0, remaining);
                            overflow.set(true);
                        } else retained.write(chunk, 0, count);
                    }
                    if (overflow.get()) break;
                }
            } catch (IOException ignored) {
                // Closing the stream is expected after owned-process cleanup.
            }
        });

        boolean timedOut = false;
        IOException cleanupFailure = null;
        try {
            timedOut = !child.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (timedOut || overflow.get()) terminateOwned(pid, root, child, owned);
            if (child.isAlive() && !child.waitFor(5, TimeUnit.SECONDS))
                throw new IOException("owned child did not terminate after cleanup");

            // A successful root may have returned while leaving a detached/background child.
            // It is still owned by this invocation and must not outlive the verifier.
            monitoring.set(false);
            monitor.join(5000);
            if (monitor.isAlive()) throw new IOException("owned process-tree monitor did not terminate");
            rememberDescendants(root, owned);
            terminateOwnedDescendants(pid, owned);
            reader.join(5000);
            if (reader.isAlive()) throw new IOException("owned output reader did not terminate");
            assertNoOwnedDescendants(owned);
            int exit = child.exitValue();
            synchronized (retained) {
                return new Result(pid, exit, timedOut, overflow.get(), retained.toString(java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (IOException failure) {
            cleanupFailure = failure;
            throw failure;
        } finally {
            monitoring.set(false);
            if (child.isAlive()) child.destroyForcibly();
            try {
                if (!child.waitFor(5, TimeUnit.SECONDS)) {
                    if (cleanupFailure == null) throw new IOException("owned child cleanup failed");
                }
                if (monitor.isAlive()) monitor.join(5000);
                rememberDescendants(root, owned);
                terminateOwnedDescendants(pid, owned);
                if (reader.isAlive()) {
                    child.getInputStream().close();
                    reader.interrupt();
                    reader.join(5000);
                }
                if (reader.isAlive() || monitor.isAlive()) {
                    if (cleanupFailure == null) throw new IOException("owned process helper thread cleanup failed");
                }
                assertNoOwnedDescendants(owned);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                if (cleanupFailure == null) throw interrupted;
            }
        }
    }

    private static void rememberDescendants(ProcessHandle root, Map<Long, ProcessHandle> owned) {
        root.descendants().forEach(handle -> owned.putIfAbsent(handle.pid(), handle));
    }

    private static void terminateOwned(long processGroup, ProcessHandle root, Process child, Map<Long, ProcessHandle> owned)
            throws IOException, InterruptedException {
        rememberDescendants(root, owned);
        for (ProcessHandle handle : owned.values()) if (handle.isAlive()) handle.destroy();
        signalGroup(processGroup, "-TERM");
        if (root.isAlive()) root.destroy();
        awaitOwned(owned, 2);
        for (ProcessHandle handle : owned.values()) if (handle.isAlive()) handle.destroyForcibly();
        signalGroup(processGroup, "-KILL");
        if (root.isAlive()) child.destroyForcibly();
        awaitOwned(owned, 3);
        if (child.isAlive() && !child.waitFor(5, TimeUnit.SECONDS))
            throw new IOException("owned process tree did not terminate");
        awaitGroupGone(processGroup, 5);
        assertNoOwnedDescendants(owned);
    }

    private static void terminateOwnedDescendants(long processGroup, Map<Long, ProcessHandle> owned) throws IOException, InterruptedException {
        for (ProcessHandle handle : owned.values()) if (handle.isAlive()) handle.destroy();
        signalGroup(processGroup, "-TERM");
        awaitOwned(owned, 2);
        for (ProcessHandle handle : owned.values()) if (handle.isAlive()) handle.destroyForcibly();
        signalGroup(processGroup, "-KILL");
        awaitOwned(owned, 3);
        awaitGroupGone(processGroup, 5);
        assertNoOwnedDescendants(owned);
    }

    private static void signalGroup(long group, String signal) throws IOException, InterruptedException {
        Process signaler = new ProcessBuilder("/usr/bin/kill", signal, "--", "-" + group).start();
        if (!signaler.waitFor(2, TimeUnit.SECONDS)) {
            signaler.destroyForcibly();
            if (!signaler.waitFor(2, TimeUnit.SECONDS)) throw new IOException("process-group signal helper did not terminate");
        }
        // kill returns 1 for an already empty group; that is a normal cleanup state.
    }

    private static void awaitGroupGone(long group, int seconds) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            if (!processGroupRunning(group)) return;
            Thread.sleep(20);
        }
        throw new IOException("owned process group remains alive: " + group);
    }

    private static void awaitOwned(Map<Long, ProcessHandle> owned, int seconds) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (owned.values().stream().anyMatch(handle -> processRunning(handle.pid()))) {
            if (System.nanoTime() >= deadline) throw new IOException("owned descendant cleanup timed out");
            Thread.sleep(10);
        }
    }

    private static void assertNoOwnedDescendants(Map<Long, ProcessHandle> owned) throws IOException {
        var survivors = owned.values().stream().filter(handle -> processRunning(handle.pid())).map(ProcessHandle::pid).toList();
        if (!survivors.isEmpty()) throw new IOException("owned descendants remain alive: " + survivors);
    }

    private static boolean processRunning(long pid) {
        try {
            String stat = Files.readString(Path.of("/proc", Long.toString(pid), "stat"));
            int endCommand = stat.lastIndexOf(')');
            if (endCommand < 0) return false;
            String[] fields = stat.substring(endCommand + 1).trim().split("\\s+");
            return fields.length > 0 && !fields[0].equals("Z") && !fields[0].equals("X");
        } catch (IOException absentOrInaccessible) {
            return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
        }
    }

    private static boolean processGroupRunning(long group) throws IOException {
        try (var entries = Files.list(Path.of("/proc"))) {
            for (Path entry : entries.filter(path -> path.getFileName().toString().matches("[0-9]+")).toList()) {
                try {
                    String stat = Files.readString(entry.resolve("stat"));
                    int endCommand = stat.lastIndexOf(')');
                    if (endCommand < 0) continue;
                    String[] fields = stat.substring(endCommand + 1).trim().split("\\s+");
                    if (fields.length >= 3 && fields[2].equals(Long.toString(group))
                            && !fields[0].equals("Z") && !fields[0].equals("X")) return true;
                } catch (IOException vanished) {
                    // Process exited after the /proc listing.
                }
            }
        }
        return false;
    }

    public record Result(long pid, int exitCode, boolean timedOut, boolean outputOverflow, String capturedOutput) {
        public boolean successful() { return exitCode == 0 && !timedOut && !outputOverflow; }
    }
}
