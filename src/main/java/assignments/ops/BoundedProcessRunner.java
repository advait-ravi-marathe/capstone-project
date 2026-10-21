package assignments.ops;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Instructor-supplied bounded child-process plumbing for release verification. */
public final class BoundedProcessRunner {
    public Result run(List<String> command, Duration timeout, int maxOutputBytes) throws IOException, InterruptedException {
        return run(command, java.util.Map.of(), timeout, maxOutputBytes);
    }
    public Result run(List<String> command, java.util.Map<String,String> environment, Duration timeout, int maxOutputBytes) throws IOException, InterruptedException {
        if (command == null || command.isEmpty() || timeout.isZero() || timeout.isNegative() || maxOutputBytes < 1)
            throw new IllegalArgumentException("invalid bounded process request");
        ProcessBuilder builder=new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().putAll(environment);
        Process child = builder.start();
        long pid = child.pid();
        ByteArrayOutputStream retained = new ByteArrayOutputStream(Math.min(maxOutputBytes, 8192));
        AtomicBoolean overflow = new AtomicBoolean();
        java.util.concurrent.atomic.AtomicReference<java.util.List<ProcessHandle>> overflowDescendants = new java.util.concurrent.atomic.AtomicReference<>(List.of());
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
                    if (overflow.get()) {
                        var descendants = child.toHandle().descendants().toList();
                        overflowDescendants.set(descendants);
                        // Let the live parent reap its children before the root is terminated.
                        descendants.forEach(ProcessHandle::destroy);
                        break;
                    }
                }
            } catch (IOException ignored) {
                // Process termination closes the stream; exit/timeout remains the authoritative outcome.
            }
        });
        boolean timedOut = false;
        try {
            timedOut = !child.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (timedOut) terminateTree(child);
            if (overflow.get()) awaitProcesses(overflowDescendants.get());
            if (child.isAlive() && !child.waitFor(5, TimeUnit.SECONDS))
                throw new IOException("owned child did not terminate after forcible cleanup");
            if (!child.waitFor(5, TimeUnit.SECONDS)) throw new IOException("owned child termination was not observed");
            reader.join(5000);
            if (reader.isAlive()) throw new IOException("owned output reader did not terminate");
            int exit = child.exitValue();
            synchronized (retained) {
                return new Result(pid, exit, timedOut, overflow.get(), retained.toString(java.nio.charset.StandardCharsets.UTF_8));
            }
        } finally {
            if (child.isAlive()) {
                terminateTree(child);
                if (!child.waitFor(5, TimeUnit.SECONDS)) throw new IOException("owned child cleanup failed");
            }
            reader.interrupt();
        }
    }
    private static void terminateTree(Process child) {
        var descendants = child.toHandle().descendants().toList();
        for (var process : descendants) process.destroy();
        awaitProcesses(descendants, 2);
        for (var process : descendants) if (process.isAlive()) process.destroyForcibly();
        awaitProcesses(descendants, 3);
        if (child.isAlive()) child.destroyForcibly();
    }
    private static void awaitProcesses(java.util.List<ProcessHandle> processes) { awaitProcesses(processes,5); }
    private static void awaitProcesses(java.util.List<ProcessHandle> processes,int seconds) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        for (var process : processes) {
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) try { process.onExit().get(remaining, TimeUnit.NANOSECONDS); }
            catch (Exception ignored) { }
        }
    }
    public record Result(long pid, int exitCode, boolean timedOut, boolean outputOverflow, String capturedOutput) {
        public boolean successful() { return exitCode == 0 && !timedOut && !outputOverflow; }
    }
}
