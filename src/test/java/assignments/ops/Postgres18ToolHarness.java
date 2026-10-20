package assignments.ops;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Instructor-owned adapter to pinned official PostgreSQL 18.6 client tools in Docker. */
final class Postgres18ToolHarness {
    static final String IMAGE = "postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336";
    private static final Pattern PORT = Pattern.compile("(?:127\\.0\\.0\\.1|localhost):(\\d+)/");

    static String toolVersion(String tool) throws Exception {
        Result result = run(List.of("docker", "run", "--rm", "--network", "host", "--entrypoint", tool,
                IMAGE, "--version"), null, null, 15);
        Assertions.assertEquals(0, result.exitCode, result.output);
        return result.output.trim();
    }

    static String createFreshTarget(EmbeddedPostgres pg) throws Exception {
        String database = "restore_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = pg.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE \"" + database + "\" WITH TEMPLATE template0");
        }
        return database;
    }

    static Path dump(EmbeddedPostgres pg) throws Exception {
        Path archive = Files.createTempFile("coverstory-release-", ".dump");
        int port = port(pg);
        List<String> command = List.of("docker", "run", "--rm", "--network", "host", "--entrypoint", "pg_dump",
                IMAGE, "--format=custom", "--no-password", "--host=127.0.0.1", "--port=" + port,
                "--username=postgres", "--dbname=postgres");
        try {
            Result result = run(command, null, archive, 60);
            Assertions.assertEquals(0, result.exitCode, result.output);
            Assertions.assertTrue(Files.size(archive) > 0, "pg_dump must create a non-empty custom archive");
            return archive;
        } catch (Exception | Error failure) {
            Files.deleteIfExists(archive);
            throw failure;
        }
    }

    static void restore(EmbeddedPostgres pg, Path archive, String freshTarget) throws Exception {
        Result result = restoreResult(pg, archive, freshTarget);
        Assertions.assertEquals(0, result.exitCode, result.output);
    }

    static Result restoreResult(EmbeddedPostgres pg, Path archive, String freshTarget) throws Exception {
        int port = port(pg);
        List<String> command = List.of("docker", "run", "--rm", "--interactive", "--network", "host",
                "--entrypoint", "pg_restore", IMAGE, "--verbose", "--exit-on-error", "--single-transaction",
                "--no-owner", "--no-acl", "--host=127.0.0.1", "--port=" + port,
                "--username=postgres", "--dbname=" + freshTarget);
        return run(command, archive, null, 90);
    }

    static Result listArchive(EmbeddedPostgres pg, Path archive) throws Exception {
        Path output = Files.createTempFile("coverstory-pg-archive-list-", ".txt");
        try {
            Result result = run(List.of("docker", "run", "--rm", "--interactive", "--network", "host",
                    "--entrypoint", "pg_restore", IMAGE, "--list"), archive, output, 30);
            return new Result(result.exitCode(), result.output() + Files.readString(output));
        } finally {
            Files.deleteIfExists(output);
        }
    }

    static void timeoutCleanupPreservesUnrelatedSentinel() throws Exception {
        String sentinel = "coverstory-sentinel-" + UUID.randomUUID().toString().replace("-", "");
        Process start = new ProcessBuilder("docker", "run", "--detach", "--name", sentinel, "--network", "none",
                IMAGE, "sh", "-c", "sleep 60").redirectErrorStream(true).start();
        Assertions.assertTrue(start.waitFor(20, TimeUnit.SECONDS), "sentinel fixture startup watchdog");
        Assertions.assertEquals(0, start.exitValue(), new String(start.getInputStream().readAllBytes()));
        try {
            Assertions.assertThrows(AssertionError.class, () -> run(List.of("docker", "run", "--rm", "--network", "none",
                    "--entrypoint", "sh", IMAGE, "-c", "sleep 60"), null, null, 1));
            Process inspect = new ProcessBuilder("docker", "inspect", "--format={{.State.Running}}", sentinel)
                    .redirectErrorStream(true).start();
            Assertions.assertTrue(inspect.waitFor(10, TimeUnit.SECONDS), "sentinel inspection watchdog");
            Assertions.assertEquals(0, inspect.exitValue());
            Assertions.assertEquals("true", new String(inspect.getInputStream().readAllBytes()).trim(),
                    "watchdog cleanup may remove only the generated tool container, not an unrelated sentinel");
        } finally {
            Process cleanup = new ProcessBuilder("docker", "rm", "--force", sentinel).redirectErrorStream(true).start();
            Assertions.assertTrue(cleanup.waitFor(10, TimeUnit.SECONDS), "owned sentinel cleanup watchdog");
            Assertions.assertEquals(0, cleanup.exitValue());
        }
    }

    static void dropOwnedTarget(EmbeddedPostgres pg, String database) throws Exception {
        Assertions.assertTrue(database.matches("restore_[0-9a-f]{32}"), "only this run's generated target may be removed");
        try (Connection connection = pg.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS \"" + database + "\"");
        }
    }

    private static int port(EmbeddedPostgres pg) {
        Matcher matcher = PORT.matcher(pg.getJdbcUrl("postgres", "postgres"));
        if (!matcher.find()) throw new IllegalStateException("fixture JDBC URL has no loopback port");
        return Integer.parseInt(matcher.group(1));
    }

    private static Result run(List<String> command, Path input, Path output, int timeoutSeconds) throws Exception {
        Path log = Files.createTempFile("coverstory-pgtool-", ".log");
        Path stdout = output == null ? Files.createTempFile("coverstory-pgtool-", ".out") : output;
        List<String> actualCommand = new java.util.ArrayList<>(command);
        String ownedContainer = null;
        if (actualCommand.size() > 2 && actualCommand.get(0).equals("docker") && actualCommand.get(1).equals("run")) {
            ownedContainer = "coverstory-pgtool-" + UUID.randomUUID().toString().replace("-", "");
            actualCommand.add(2, "--name");
            actualCommand.add(3, ownedContainer);
        }
        ProcessBuilder builder = new ProcessBuilder(actualCommand).redirectError(log.toFile()).redirectOutput(stdout.toFile());
        if (input != null) builder.redirectInput(input.toFile());
        Process process = builder.start();
        try {
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Assertions.assertTrue(process.waitFor(5, TimeUnit.SECONDS), "PostgreSQL tool process must terminate after watchdog");
                if (ownedContainer != null) stopOwnedContainer(ownedContainer);
                throw new AssertionError("PostgreSQL tool watchdog expired: " + String.join(" ", actualCommand));
            }
            String details = Files.readString(log);
            if (output == null && Files.size(stdout) > 0) details += Files.readString(stdout);
            return new Result(process.exitValue(), details);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                Assertions.assertTrue(process.waitFor(5, TimeUnit.SECONDS), "owned PostgreSQL tool process termination");
            }
            Files.deleteIfExists(log);
            if (output == null) Files.deleteIfExists(stdout);
        }
    }

    private static void stopOwnedContainer(String name) throws Exception {
        Assertions.assertTrue(name.matches("coverstory-pgtool-[0-9a-f]{32}"), "cleanup is restricted to the run-owned container name");
        Process stop = new ProcessBuilder("docker", "stop", "--time", "2", name).redirectErrorStream(true).start();
        Assertions.assertTrue(stop.waitFor(10, TimeUnit.SECONDS), "owned Docker container stop watchdog");
        if (stop.exitValue() != 0) {
            Process remove = new ProcessBuilder("docker", "rm", "--force", name).redirectErrorStream(true).start();
            Assertions.assertTrue(remove.waitFor(10, TimeUnit.SECONDS), "owned Docker container removal watchdog");
        }
        Process inspect = new ProcessBuilder("docker", "inspect", name).redirectErrorStream(true).start();
        Assertions.assertTrue(inspect.waitFor(10, TimeUnit.SECONDS), "owned Docker container cleanup verification watchdog");
        Assertions.assertNotEquals(0, inspect.exitValue(), "timed-out run-owned container must be removed");
    }

    record Result(int exitCode, String output) {}
    private Postgres18ToolHarness() {}
}
