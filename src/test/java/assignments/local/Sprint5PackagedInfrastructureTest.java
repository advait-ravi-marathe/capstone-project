package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

/** Fresh archive/process evidence for Sprint 5; separate from the later default local runtime. */
@Tag("learnerAcceptance")
class Sprint5PackagedInfrastructureTest {
    @Test
    void day67FreshSprint5ArchiveHasDedicatedEntryAndNormalAssets() throws Exception {
        Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint5.jar");
        Path receipt = Path.of("target/sprint5-demo/package-evidence.json");
        assertTrue(Files.isRegularFile(jar), "Run scripts/package-sprint5-demo.sh first");
        var evidence = new ObjectMapper().readTree(Files.readString(receipt));
        assertEquals(sha256(jar), evidence.path("artifactSha256").asString());
        try (JarFile archive = new JarFile(jar.toFile())) {
            assertEquals("assignments.sprint5.Sprint5LocalApplication",
                    archive.getManifest().getMainAttributes().getValue("Start-Class"));
            assertNotNull(archive.getEntry("BOOT-INF/classes/static/index.html"));
            assertTrue(archive.stream().anyMatch(e -> e.getName().startsWith("BOOT-INF/classes/static/assets/")));
            assertNotNull(archive.getEntry("BOOT-INF/classes/application-sprint5-local.properties"));
        }
        String entry = Files.readString(Path.of("frontend/src/local-main.tsx"));
        assertFalse(entry.contains("__e2e"));
        assertFalse(entry.contains("/test/"));
    }

    @Test
    void stageEntryRejectsWildcardBindBeforeStartingTheServer() throws Exception {
        Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint5.jar").toAbsolutePath();
        Path output = Path.of("target/sprint5-demo/non-loopback-guard.log").toAbsolutePath();
        Files.deleteIfExists(output);
        assertTrue(Files.isRegularFile(jar), "Run scripts/package-sprint5-demo.sh first");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(javaExecutable, "-jar", jar.toString(), "--server.address=0.0.0.0",
                "--server.port=0").redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS), "guard must fail before listener startup");
            assertNotEquals(0, process.exitValue());
            String diagnostic = Files.readString(output);
            assertTrue(diagnostic.contains("127.0.0.1") || diagnostic.contains("loopback"),
                    "failure output should identify the bind restriction");
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor();
            }
        }
    }

    @Test
    void day67Sprint5ArchiveServesOnlyTheBoundedPackagedSurface() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             Sprint5PackagedRuntimeProcess app = Sprint5PackagedRuntimeProcess.start(postgres,
                     "2026-12-19T12:00:00Z")) {
            assertTrue(app.pid() > 0);
            assertEquals(200, app.get("/").statusCode());
            assertEquals(200, app.get("/actuator/health").statusCode());
            assertEquals(401, app.get("/api/session").statusCode());
            assertEquals(401, app.get("/test/day44").statusCode(), "test path is protected and has no public test response");
            assertEquals(401, app.get("/__e2e/day44-lifecycle").statusCode(), "e2e path is protected and has no public test response");
            assertEquals(3, scalar(postgres, "SELECT count(*) FROM flyway_schema_history WHERE success"));
            assertEquals(0, scalar(postgres, "SELECT count(*) FROM flyway_schema_history WHERE version='4'"));
            assertEquals(0, scalar(postgres, "SELECT count(*) FROM flyway_schema_history WHERE version='5'"));
        }
    }

    private static int scalar(EmbeddedPostgres postgres, String sql) throws Exception {
        try (var c = postgres.getPostgresDatabase().getConnection(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            assertTrue(r.next());
            return r.getInt(1);
        }
    }

    private static String sha256(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
}
