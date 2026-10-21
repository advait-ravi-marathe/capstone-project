package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import tools.jackson.databind.ObjectMapper;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Starts only the freshly packaged, explicitly bounded Sprint 5 archive. */
final class Sprint5PackagedRuntimeProcess implements AutoCloseable {
    private final Process process;
    private final int port;
    private final Path log;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();

    private Sprint5PackagedRuntimeProcess(Process process, int port, Path log) {
        this.process = process;
        this.port = port;
        this.log = log;
    }

    static Sprint5PackagedRuntimeProcess start(EmbeddedPostgres postgres, String fixedClock) throws Exception {
        Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint5.jar").toAbsolutePath();
        Path evidence = Path.of("target/sprint5-demo/package-evidence.json");
        if (!Files.isRegularFile(jar) || !Files.isRegularFile(evidence)) {
            throw new IllegalStateException("Build a fresh Sprint 5 archive with scripts/package-sprint5-demo.sh first.");
        }
        String expected = new ObjectMapper().readTree(Files.readString(evidence)).path("artifactSha256").asString();
        if (!expected.equals(sha256(jar))) throw new IllegalStateException("Sprint 5 archive no longer matches its build receipt.");
        int port;
        try (ServerSocket socket = new ServerSocket(0, 0, java.net.InetAddress.getByName("127.0.0.1"))) {
            port = socket.getLocalPort();
        }
        Path log = Path.of("target/sprint5-demo/process-" + port + ".log").toAbsolutePath();
        Files.createDirectories(log.getParent());
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>(List.of(java, "-jar", jar.toString(),
                "--spring.profiles.active=local,sprint5-local", "--server.address=127.0.0.1",
                "--server.port=" + port, "--logging.level.root=ERROR",
                "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                "--spring.datasource.username=postgres", "--spring.datasource.password="));
        if (fixedClock != null) command.add("--coverstory.local.fixed-clock=" + fixedClock);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        var runtime = new Sprint5PackagedRuntimeProcess(process, port, log);
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

    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().timeout(Duration.ofSeconds(3)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }

    private void waitUntilReady() throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(35).toNanos();
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) throw new IllegalStateException("Sprint 5 JAR exited early:\n" + Files.readString(log));
            try { if (get("/actuator/health").statusCode() == 200) return; }
            catch (Exception ignored) { }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Sprint 5 JAR did not become ready:\n" + Files.readString(log));
    }

    private static String sha256(Path path) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        return java.util.HexFormat.of().formatHex(digest);
    }

    @Override public void close() throws Exception {
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) { process.destroyForcibly(); process.waitFor(); }
    }
}
