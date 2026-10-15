package coverstory.modules.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import coverstory.CoverStoryApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Real Spring HTTP + Application-owned PostgreSQL reads, with test-only synthetic session/adapter. */
class CAP54HttpRuntimeIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String JOURNEY_A = "3c7d76dc-29dd-4f2c-a979-8c4a40c8a001";
    private static final String JOURNEY_B = "3c7d76dc-29dd-4f2c-a979-8c4a40c8a002";
    private static final String MISSING = "00000000-0000-4000-8000-000000000099";

    @Test void exactCAP54SavedAcceptanceCasesUseApplicationOwnedPostgresAndActualHttp() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            String url, user;
            try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                url = c.getMetaData().getURL(); user = c.getMetaData().getUserName();
                sql(s, "db/schema/ownership.sql"); sql(s, "db/schema/constraints.sql"); sql(s, "db/schema/cap78_purchase.sql");
                s.execute("""
                    INSERT INTO application.application_record(application_id,owner_id,revision,quote_id,status) VALUES
                    ('8b15296a-6a8c-4b71-92d5-0749a760a001','3c7d76dc-29dd-4f2c-a979-8c4a4000a001',2,'8b15296a-6a8c-4b71-92d5-0749a760b001','DRAFT'),
                    ('8b15296a-6a8c-4b71-92d5-0749a760a002','3c7d76dc-29dd-4f2c-a979-8c4a4000a002',1,'8b15296a-6a8c-4b71-92d5-0749a760b002','SUBMITTED_PENDING_DISPATCH');
                    INSERT INTO application.journey(journey_id,owner_id,application_id,revision,quote_id,status,current_step) VALUES
                    ('3c7d76dc-29dd-4f2c-a979-8c4a40c8a001','3c7d76dc-29dd-4f2c-a979-8c4a4000a001','8b15296a-6a8c-4b71-92d5-0749a760a001',2,'8b15296a-6a8c-4b71-92d5-0749a760b001','DRAFT','DISCOVER'),
                    ('3c7d76dc-29dd-4f2c-a979-8c4a40c8a002','3c7d76dc-29dd-4f2c-a979-8c4a4000a002','8b15296a-6a8c-4b71-92d5-0749a760a002',1,'8b15296a-6a8c-4b71-92d5-0749a760b002','SUBMITTED_PENDING_DISPATCH','ONBOARDING');
                    """);
            }
            try (ConfigurableApplicationContext app = start(url, user)) {
                JdbcCAP54ResumeReader applicationReader = app.getBean(JdbcCAP54ResumeReader.class);
                var ownedLink = applicationReader.findOwned(CAP54InstructorResumeOracle.OWNER_A.toString(), java.util.UUID.fromString(JOURNEY_A)).orElseThrow();
                assertEquals(2, ownedLink.revision(), "Application joins only the journey-linked revision");
                assertTrue(applicationReader.findOwned(CAP54InstructorResumeOracle.OWNER_B.toString(), java.util.UUID.fromString(JOURNEY_A)).isEmpty());
                assertEquals("DRAFT", applicationReader.load(ownedLink.applicationId(), ownedLink.revision()).orElseThrow().status());
                assertThrows(UnsupportedOperationException.class, () -> app.getBean(coverstory.modules.application.api.CAP54Seam.class)
                        .resume(new coverstory.modules.application.api.CAP54Seam.Principal(CAP54InstructorResumeOracle.OWNER_A.toString()),
                                new coverstory.modules.application.api.CAP54Seam.ResumeRequest(java.util.UUID.fromString(JOURNEY_A), null, "ISSUED")));
                CAP54InstructorResumeOracle oracle = app.getBean(CAP54InstructorResumeOracle.class);
                assertEquals(200, oracle.resume(CAP54InstructorResumeOracle.OWNER_A.toString(), java.util.UUID.fromString(JOURNEY_A)).status());
                oracle.resetCounts();
                assertEquals(200, get(app, "/api/session", "synthetic-session-a").statusCode());
                HttpResponse<String> own = get(app, "/api/v1/me/journeys/"+JOURNEY_A+"/resume?current_step=ISSUED", "synthetic-session-a");
                assertEquals(200, own.statusCode(), own.body());
                assertEquals(JOURNEY_A, JSON.readTree(own.body()).path("journeyId").asText());
                assertEquals("DRAFT", JSON.readTree(own.body()).path("status").asText());
                assertEquals("CONTINUE_CHECKOUT", JSON.readTree(own.body()).path("resumeAction").asText());
                assertFalse(own.body().contains("SYNTHETIC_PRIVATE_SENTINEL"));
                oracle.resetCounts();

                HttpResponse<String> foreign = get(app, "/api/v1/me/journeys/"+JOURNEY_A+"/resume", "synthetic-session-b");
                HttpResponse<String> absent = get(app, "/api/v1/me/journeys/"+MISSING+"/resume", "synthetic-session-a");
                assertEquals(404, foreign.statusCode()); assertEquals(404, absent.statusCode());
                assertEquals(JSON.readTree(absent.body()), JSON.readTree(foreign.body()));
                assertEquals(0, app.getBean(CAP54InstructorResumeOracle.class).acceptedStateReads());

                oracle.resetCounts();
                HttpResponse<String> override = get(app, "/api/v1/me/journeys/"+JOURNEY_A+"/resume?owner_user_id=other&current_step=ISSUED", "synthetic-session-a");
                assertEquals(400, override.statusCode());
                assertEquals("INVALID_OWNER_OVERRIDE", JSON.readTree(override.body()).path("code").asText());
                assertEquals(0, oracle.repositoryCalls(), "an owner override is rejected before any repository read");

                HttpResponse<String> listA = get(app, "/api/v1/me/journeys?limit=50", "synthetic-session-a");
                assertEquals(200, listA.statusCode());
                JsonNode rowsA = JSON.readTree(listA.body()).path("journeys");
                assertEquals(1, rowsA.size()); assertEquals(JOURNEY_A, rowsA.get(0).path("journeyId").asText());
                HttpResponse<String> listB = get(app, "/api/v1/me/journeys?limit=50", "synthetic-session-b");
                assertEquals(JOURNEY_B, JSON.readTree(listB.body()).path("journeys").get(0).path("journeyId").asText());
                assertEquals(400, get(app, "/api/v1/me/journeys?limit=51", "synthetic-session-a").statusCode());
                assertEquals(200, get(app, "/api/v1/me/journeys?limit=50&cursor="+CAP54InstructorResumeOracle.OWNER_A+"."+JOURNEY_A, "synthetic-session-a").statusCode());
                assertEquals(400, get(app, "/api/v1/me/journeys?limit=50&cursor="+CAP54InstructorResumeOracle.OWNER_B+"."+JOURNEY_B, "synthetic-session-a").statusCode());

                assertEquals(401, get(app, "/api/v1/me/journeys", null).statusCode());
                assertEquals(401, get(app, "/api/session", null).statusCode());
                HttpResponse<String> session = get(app, "/api/session", "synthetic-session-a");
                assertEquals(200, session.statusCode());
                assertEquals(CAP54InstructorResumeOracle.OWNER_A.toString(), JSON.readTree(session.body()).path("ownerId").asText());
                assertEquals(200, get(app, "/api/catalog?category=health", null).statusCode(), "anonymous public catalog remains available");
            }
            if (Boolean.getBoolean("coverstory.cap54.browser")) {
                try (ConfigurableApplicationContext app = start(url, user)) {
                    runBrowser(((WebServerApplicationContext) app).getWebServer().getPort());
                }
            }
        }
    }

    private static ConfigurableApplicationContext start(String url, String username) {
        return new SpringApplicationBuilder(CoverStoryApplication.class, CAP54InstructorRuntimeConfiguration.class, ResumeOracleConfiguration.class)
                .web(WebApplicationType.SERVLET).properties("server.port=0", "spring.main.banner-mode=off",
                        "logging.level.root=ERROR", "spring.profiles.active=cap54-runtime-test", "coverstory.application.jdbc.url="+url,
                        "coverstory.application.jdbc.username="+username, "coverstory.application.jdbc.password=")
                .run();
    }

    @org.springframework.context.annotation.Configuration
    @org.springframework.context.annotation.Profile("cap54-runtime-test")
    static class ResumeOracleConfiguration {
        @Bean @Primary CAP54InstructorResumeOracle cap54InstructorResumeOracle(
                @org.springframework.beans.factory.annotation.Qualifier("applicationResumeJdbcTemplate") JdbcTemplate jdbc) {
            return new CAP54InstructorResumeOracle(jdbc);
        }
    }

    private static HttpResponse<String> get(ConfigurableApplicationContext app, String path, String session) throws Exception {
        int port = ((WebServerApplicationContext) app).getWebServer().getPort();
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Accept", "application/json");
        if (session != null) builder.header("Cookie", "cap54_test_session="+session);
        return HttpClient.newHttpClient().send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void sql(Statement statement, String path) throws Exception {
        statement.execute(Files.readString(Path.of("src/main/resources", path)));
    }

    private static void runBrowser(int apiPort) throws Exception {
        Process browser = new ProcessBuilder("node", "scripts/cap54-browser-flow.mjs", "http://127.0.0.1:"+apiPort)
                .directory(Path.of("web").toFile()).redirectErrorStream(true).start();
        boolean passed = false;
        try (BufferedReader output = new BufferedReader(new InputStreamReader(browser.getInputStream()))) {
            CompletableFuture<String> line = CompletableFuture.supplyAsync(() -> { try { return output.readLine(); } catch (Exception e) { throw new RuntimeException(e); } });
            long deadline = System.nanoTime()+Duration.ofSeconds(90).toNanos(); boolean pass=false;
            while (System.nanoTime()<deadline) {
                String value=line.get(Math.max(1,deadline-System.nanoTime()),TimeUnit.NANOSECONDS);
                if(value==null)break;
                if(value.startsWith("CAP54_BROWSER_FLOW ")) { System.out.println(value); pass=true; break; }
                if(value.startsWith("Error:")||value.startsWith("AssertionError")) {
                    browser.waitFor(10, TimeUnit.SECONDS);
                    StringBuilder details = new StringBuilder(value);
                    String remaining;
                    while ((remaining = output.readLine()) != null) details.append('\n').append(remaining);
                    throw new AssertionError("CAP54 browser witness failed: "+details);
                }
                line=CompletableFuture.supplyAsync(() -> { try { return output.readLine(); } catch (Exception e) { throw new RuntimeException(e); } });
            }
            if(!pass)throw new AssertionError("CAP54 browser witness timed out");
            passed = true;
        } finally {
            if (!passed && browser.isAlive()) browser.destroyForcibly();
        }
        if (!browser.waitFor(20, TimeUnit.SECONDS)) {
            browser.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
            browser.destroyForcibly();
            fail("CAP54 browser witness reported success but did not finish cleanup");
        }
        assertEquals(0, browser.exitValue());
    }
}
