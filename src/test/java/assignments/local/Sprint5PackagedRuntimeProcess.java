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
        verifyArtifactReceipt(jar, evidence);
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

    HttpResponse<String> getWithCookie(String path, String cookie) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).header("Cookie", cookie).GET()
                        .timeout(Duration.ofSeconds(3)).build(), HttpResponse.BodyHandlers.ofString());
    }

    String selectOwnerA() throws Exception {
        var csrfResponse = get("/api/csrf");
        if (csrfResponse.statusCode() != 200) throw new IllegalStateException("Could not obtain local CSRF token.");
        String token = new ObjectMapper().readTree(csrfResponse.body()).path("token").asString();
        String csrfCookie = csrfResponse.headers().allValues("set-cookie").stream()
                .filter(value -> value.startsWith("XSRF-TOKEN="))
                .map(value -> value.substring("XSRF-TOKEN=".length(), value.indexOf(';') < 0 ? value.length() : value.indexOf(';')))
                .findFirst().orElseThrow(() -> new IllegalStateException("CSRF cookie was not issued."));
        var selection = client.send(HttpRequest.newBuilder(uri("/api/session"))
                        .header("Cookie", "XSRF-TOKEN=" + csrfCookie)
                        .header("X-XSRF-TOKEN", token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"ownerId\":\"owner-a\"}"))
                        .timeout(Duration.ofSeconds(3)).build(), HttpResponse.BodyHandlers.ofString());
        if (selection.statusCode() != 200) throw new IllegalStateException("Could not select synthetic owner-a.");
        return selection.headers().allValues("set-cookie").stream()
                .filter(value -> value.startsWith("JSESSIONID="))
                .map(value -> value.substring(0, value.indexOf(';') < 0 ? value.length() : value.indexOf(';')))
                .findFirst().orElseThrow(() -> new IllegalStateException("Session cookie was not issued."));
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

    static String git(String... arguments) throws Exception {
        var command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Could not verify current source identity: " + output);
        }
        return output;
    }

    static String sourceFingerprint() throws Exception {
        Process process = new ProcessBuilder("python3", "scripts/sprint5-source-fingerprint.py")
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Could not fingerprint current Sprint 5 source: " + output);
        }
        return output;
    }

    static void verifyArtifactReceipt(Path jar, Path evidence) throws Exception {
        var receipt = new ObjectMapper().readTree(Files.readString(evidence));
        if (!receipt.path("sourceCommit").asString().equals(git("rev-parse", "HEAD"))) {
            throw new IllegalStateException("Sprint 5 receipt belongs to a different source commit.");
        }
        if (!receipt.path("sourceContentSha256").asString().equals(sourceFingerprint())) {
            throw new IllegalStateException("Current source contents differ from the Sprint 5 package receipt.");
        }
        if (!receipt.path("artifactSha256").asString().equals(sha256(jar))) {
            throw new IllegalStateException("Sprint 5 archive no longer matches its build receipt.");
        }
    }

    @Override public void close() throws Exception {
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) { process.destroyForcibly(); process.waitFor(); }
    }
}
