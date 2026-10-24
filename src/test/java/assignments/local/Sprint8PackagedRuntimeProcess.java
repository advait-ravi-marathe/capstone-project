package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Starts the actual executable archive against a disposable PostgreSQL instance. */
final class Sprint8PackagedRuntimeProcess implements AutoCloseable {
    private final Process process;
    private final int port;
    private final Path log;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();

    private Sprint8PackagedRuntimeProcess(Process process, int port, Path log) {
        this.process = process;
        this.port = port;
        this.log = log;
    }

    static Sprint8PackagedRuntimeProcess start(EmbeddedPostgres postgres) throws Exception {
        return start(postgres, null);
    }

    static Sprint8PackagedRuntimeProcess start(EmbeddedPostgres postgres, String fixedClockInstant) throws Exception {
        return start(postgres, fixedClockInstant, Map.of());
    }

    static Sprint8PackagedRuntimeProcess start(EmbeddedPostgres postgres, String fixedClockInstant,
                                             Map<String, String> testOnlyProperties) throws Exception {
        Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint8.jar").toAbsolutePath();
        Path evidence = Path.of("target/sprint8-demo/package-evidence.json");
        if (!Files.isRegularFile(jar) || !Files.isRegularFile(evidence)) {
            throw new IllegalStateException("Build a fresh Sprint 8 archive with scripts/package-sprint8-demo.sh first.");
        }
        verifyArtifactReceipt(jar, evidence);
        int port;
        try (ServerSocket socket = new ServerSocket(0, 0, java.net.InetAddress.getByName("127.0.0.1"))) {
            port = socket.getLocalPort();
        }
        Path log = Path.of("target/sprint8-demo/packaged-process-" + port + ".log").toAbsolutePath();
        Files.createDirectories(log.getParent());
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>(List.of(java, "-jar", jar.toString(),
                "--spring.profiles.active=local,sprint8-local", "--server.address=127.0.0.1", "--server.port=" + port,
                "--logging.level.root=ERROR",
                "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                "--spring.datasource.username=postgres", "--spring.datasource.password="));
        if (fixedClockInstant != null) command.add("--coverstory.local.fixed-clock=" + fixedClockInstant);
        testOnlyProperties.forEach((key, value) -> command.add("--" + key + "=" + value));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        Sprint8PackagedRuntimeProcess runtime = new Sprint8PackagedRuntimeProcess(process, port, log);
        try {
            runtime.waitUntilReady();
            return runtime;
        } catch (Exception exception) {
            runtime.close();
            throw exception;
        }
    }

    int port() { return port; }
    long pid() { return process.pid(); }

    HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return get(path, null);
    }

    HttpResponse<String> get(String path, String cookie) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder(uri(path)).GET().timeout(Duration.ofSeconds(3));
        if (cookie != null) builder.header("Cookie", cookie);
        return client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        return post(path, body, null, null, null);
    }

    HttpResponse<String> post(String path, String body, String csrfCookie, String sessionCookie, String token)
            throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).timeout(Duration.ofSeconds(3));
        if (csrfCookie != null) {
            builder.header("Cookie", csrfCookie + (sessionCookie == null ? "" : "; " + sessionCookie));
            if (token != null) builder.header("X-XSRF-TOKEN", token);
        }
        return client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }

    private static String sha256(Path path) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        return java.util.HexFormat.of().formatHex(digest);
    }

    private static String git(String... arguments) throws Exception {
        var command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Could not verify current Sprint 8 source identity: " + output);
        }
        return output;
    }

    private static String sourceFingerprint() throws Exception {
        Process process = new ProcessBuilder("python3", "scripts/sprint8-source-fingerprint.py")
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Could not fingerprint current Sprint 8 source: " + output);
        }
        return output;
    }

    static void verifyArtifactReceipt(Path jar, Path evidence) throws Exception {
        var receipt = new ObjectMapper().readTree(Files.readString(evidence));
        if (!receipt.path("sourceCommit").asString().equals(git("rev-parse", "HEAD"))) {
            throw new IllegalStateException("Sprint 8 receipt belongs to a different source commit.");
        }
        if (!receipt.path("sourceContentSha256").asString().equals(sourceFingerprint())) {
            throw new IllegalStateException("Current source contents differ from the Sprint 8 package receipt.");
        }
        if (!receipt.path("artifactSha256").asString().equals(sha256(jar))) {
            throw new IllegalStateException("Sprint 8 archive no longer matches its build receipt.");
        }
    }

    private void waitUntilReady() throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(35).toNanos();
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) throw new IllegalStateException("Packaged JAR exited early:\n" + Files.readString(log));
            try {
                if (get("/actuator/health").statusCode() == 200) return;
            } catch (Exception ignored) { }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Packaged JAR did not become ready:\n" + Files.readString(log));
    }

    @Override public void close() throws Exception {
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor();
        }
    }
}
