package coverstory.application;

import coverstory.CoverStoryApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Live instructor-only command witness using Application's real authoritative journey row. */
class CAP60AuthoritativeRuntimeIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UUID JOURNEY = UUID.fromString("60000000-0000-4000-8000-000000000101");
    private static final UUID OWNER = UUID.fromString("60000000-0000-4000-8000-000000000102");
    private static final UUID OTHER_OWNER = UUID.fromString("60000000-0000-4000-8000-000000000103");
    private static final String ROUTE = "/api/__instructor/cap60/journeys/" + JOURNEY + "/advance";

    @Test void liveHttpRaceAndInjectedFailuresUseAuthoritativeApplicationJourneyAtomically() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            install(pg);
            try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                s.execute("INSERT INTO application.journey(journey_id,owner_id,application_id,revision,quote_id,status) VALUES ('" + JOURNEY + "','" + OWNER + "','60000000-0000-4000-8000-000000000104',4,'60000000-0000-4000-8000-000000000105','DRAFT')");
            }
            String jdbcUrl; String username;
            try (Connection c = pg.getPostgresDatabase().getConnection()) { jdbcUrl = c.getMetaData().getURL(); username = c.getMetaData().getUserName(); }

            try (ConfigurableApplicationContext app = start(jdbcUrl, username)) {
                int port = ((WebServerApplicationContext) app).getWebServer().getPort();
                HttpResponse<String> unauthenticated = post(port, null, command("01", "11"), 0);
                assertEquals(401, unauthenticated.statusCode(), unauthenticated.body());
                assertEquals(404, post(port, "owner-b", command("02", "12"), 0).statusCode());

                // Fail after each durable write boundary. Every failed command must leave the
                // Application-owned journey row and the instructor event/receipt tables unchanged.
                for (int failAfter = 1; failAfter <= 4; failAfter++) {
                    HttpResponse<String> failed = post(port, "owner-a", command(String.format("%02x", 10 + failAfter), "20"), failAfter);
                    assertEquals(503, failed.statusCode(), failed.body());
                    assertEquals("COMMAND_UNAVAILABLE", JSON.readTree(failed.body()).path("code").asText());
                    assertAuthoritativeState(pg, 4, 0, 0, 0);
                }

                String replayKey = "31".repeat(32), replayPayload = "41".repeat(32);
                List<HttpResponse<String>> replayRace = race(port, command(replayKey, replayPayload), 4);
                assertEquals(List.of(201, 201), replayRace.stream().map(HttpResponse::statusCode).sorted().toList());
                assertEquals(1, replayRace.stream().filter(r -> JSON.readTree(r.body()).path("replay").asBoolean()).count());
                assertAuthoritativeState(pg, 5, 1, 1, 1);

                HttpResponse<String> changedReplay = post(port, "owner-a", command(replayKey, "42".repeat(32)), 0);
                assertEquals(409, changedReplay.statusCode());
                assertAuthoritativeState(pg, 5, 1, 1, 1);

                List<HttpResponse<String>> revisionRace = race(port,
                        List.of(command("51".repeat(32), "61".repeat(32)), command("52".repeat(32), "62".repeat(32))), 5);
                assertEquals(1, revisionRace.stream().filter(r -> r.statusCode() == 201).count());
                assertEquals(1, revisionRace.stream().filter(r -> r.statusCode() == 409).count());
                assertAuthoritativeState(pg, 6, 3, 2, 2); // one committed key and one replay-safe conflict receipt
            }
        }
    }

    private static void install(EmbeddedPostgres pg) throws Exception {
        String source = Files.readString(Path.of("src/main/resources/db/schema/constraints.sql"));
        String marker = "CREATE TABLE IF NOT EXISTS application.journey (";
        int begin = source.indexOf(marker), end = source.indexOf(");", begin);
        assertTrue(begin >= 0 && end > begin, "Application journey DDL is present in the shared schema");
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE SCHEMA application");
            s.execute(source.substring(begin, end + 2));
            s.execute("CREATE SCHEMA cap60_harness");
            s.execute("""
                    CREATE TABLE cap60_harness.command_key(
                      journey_id UUID NOT NULL REFERENCES application.journey(journey_id),
                      key_hash BYTEA NOT NULL CHECK(octet_length(key_hash)=32),
                      payload_hash BYTEA NOT NULL CHECK(octet_length(payload_hash)=32),
                      response_status INTEGER NOT NULL CHECK(response_status BETWEEN 200 AND 499),
                      PRIMARY KEY(journey_id,key_hash))
                    """);
            s.execute("""
                    CREATE TABLE cap60_harness.audit_event(
                      event_id UUID PRIMARY KEY, journey_id UUID NOT NULL REFERENCES application.journey(journey_id),
                      revision BIGINT NOT NULL, event_code TEXT NOT NULL,
                      UNIQUE(journey_id,revision,event_code))
                    """);
            s.execute("""
                    CREATE TABLE cap60_harness.step_event(
                      event_id UUID PRIMARY KEY, journey_id UUID NOT NULL REFERENCES application.journey(journey_id),
                      revision BIGINT NOT NULL, event_code TEXT NOT NULL,
                      UNIQUE(journey_id,revision))
                    """);
        }
    }

    private static ConfigurableApplicationContext start(String url, String username) {
        return new SpringApplicationBuilder(CoverStoryApplication.class, RuntimeConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "spring.profiles.active=cap60-runtime-test", "coverstory.application.jdbc.url=" + url,
                        "coverstory.application.jdbc.username=" + username, "coverstory.application.jdbc.password=")
                .run();
    }

    private static List<HttpResponse<String>> race(int port, Map<String, String> body, long expectedRevision) throws Exception {
        return race(port, List.of(body, body), expectedRevision);
    }

    private static List<HttpResponse<String>> race(int port, List<Map<String, String>> commands, long expectedRevision) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier start = new CyclicBarrier(3);
        try {
            List<Future<HttpResponse<String>>> pending = new ArrayList<>();
            for (Map<String, String> command : commands) pending.add(pool.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                return post(port, "owner-a", command, 0, expectedRevision);
            }));
            start.await(5, TimeUnit.SECONDS);
            return List.of(pending.get(0).get(10, TimeUnit.SECONDS), pending.get(1).get(10, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
    }

    private static Map<String, String> command(String keyByte, String payloadByte) {
        String key = keyByte.length() == 2 ? keyByte.repeat(32) : keyByte;
        String payload = payloadByte.length() == 2 ? payloadByte.repeat(32) : payloadByte;
        return Map.of("keyHash", key, "payloadHash", payload);
    }

    private static HttpResponse<String> post(int port, String actor, Map<String, String> body, int failAfter) throws Exception {
        return post(port, actor, body, failAfter, 4);
    }

    private static HttpResponse<String> post(int port, String actor, Map<String, String> body, int failAfter, long expectedRevision) throws Exception {
        Map<String, Object> request = new HashMap<>(body);
        request.put("expectedRevision", expectedRevision);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + ROUTE + "?failAfter=" + failAfter))
                .timeout(Duration.ofSeconds(8)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(request)));
        if (actor != null) builder.header("X-CAP60-Test-Actor", actor);
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertAuthoritativeState(EmbeddedPostgres pg, long revision, int keys, int audit, int steps) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection()) {
            assertEquals(revision, scalar(c, "SELECT revision FROM application.journey WHERE journey_id='" + JOURNEY + "'"));
            assertEquals(keys, scalar(c, "SELECT count(*) FROM cap60_harness.command_key WHERE journey_id='" + JOURNEY + "'"));
            assertEquals(audit, scalar(c, "SELECT count(*) FROM cap60_harness.audit_event WHERE journey_id='" + JOURNEY + "'"));
            assertEquals(steps, scalar(c, "SELECT count(*) FROM cap60_harness.step_event WHERE journey_id='" + JOURNEY + "'"));
        }
    }

    private static long scalar(Connection c, String sql) throws Exception {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { assertTrue(r.next()); return r.getLong(1); }
    }

    @RestController @Profile("cap60-runtime-test")
    @RequestMapping("/api/__instructor/cap60/journeys")
    static class InstructorController {
        private final ReferenceAdapter adapter;
        InstructorController(ReferenceAdapter adapter) { this.adapter = adapter; }
        record Request(long expectedRevision, String keyHash, String payloadHash) { }
        record Response(String code, boolean replay, long authoritativeRevision) { }

        @PostMapping("/{journeyId}/advance")
        ResponseEntity<Response> advance(@PathVariable("journeyId") UUID journeyId, HttpServletRequest servletRequest, @RequestBody Request request,
                                         @RequestParam(name = "failAfter", defaultValue = "0") int failAfter) {
            Principal principal = servletRequest.getUserPrincipal();
            if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("AUTH_REQUIRED", false, 0));
            try {
                Outcome outcome = adapter.advance(journeyId, UUID.fromString(principal.getName()), request.expectedRevision(),
                        ReferenceAdapter.decodeHash(request.keyHash()), ReferenceAdapter.decodeHash(request.payloadHash()), failAfter);
                HttpStatus status = HttpStatus.valueOf(outcome.status());
                return ResponseEntity.status(status).body(new Response(outcome.replay() ? "IDEMPOTENT_REPLAY" : outcome.code(), outcome.replay(), outcome.revision()));
            } catch (InjectedFailure failure) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new Response("COMMAND_UNAVAILABLE", false, 0));
            } catch (IllegalArgumentException failure) {
                return ResponseEntity.badRequest().body(new Response("INVALID_COMMAND", false, 0));
            } catch (SQLException failure) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new Response("COMMAND_UNAVAILABLE", false, 0));
            }
        }
    }

    record Outcome(int status, boolean replay, long revision, String code) { }

    static final class ReferenceAdapter {
        private final DataSource dataSource;
        ReferenceAdapter(DataSource dataSource) { this.dataSource = dataSource; }
        Outcome advance(UUID journey, UUID owner, long expected, byte[] key, byte[] payload, int failAfter) throws SQLException {
            if (failAfter < 0 || failAfter > 4) throw new IllegalArgumentException("failAfter out of range");
            try (Connection c = dataSource.getConnection()) {
                c.setAutoCommit(false);
                try {
                    if (!owned(c, journey, owner)) { c.rollback(); return new Outcome(404, false, 0, "JOURNEY_NOT_FOUND"); }
                    int inserted;
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO cap60_harness.command_key VALUES (?, ?, ?, 201) ON CONFLICT(journey_id,key_hash) DO NOTHING")) {
                        p.setObject(1, journey); p.setBytes(2, key); p.setBytes(3, payload); inserted = p.executeUpdate();
                    }
                    if (inserted == 0) {
                        try (PreparedStatement p = c.prepareStatement("SELECT payload_hash,response_status FROM cap60_harness.command_key WHERE journey_id=? AND key_hash=?")) {
                            p.setObject(1, journey); p.setBytes(2, key);
                            try (ResultSet r = p.executeQuery()) {
                                if (!r.next()) throw new SQLException("command receipt missing after unique conflict");
                                if (!Arrays.equals(payload, r.getBytes(1))) { c.rollback(); return new Outcome(409, false, currentRevision(journey, owner), "IDEMPOTENCY_PAYLOAD_CONFLICT"); }
                                long revision = currentRevision(journey, owner); c.commit();
                                return new Outcome(r.getInt(2), true, revision, "IDEMPOTENT_REPLAY");
                            }
                        }
                    }
                    failIf(1, failAfter);
                    int changed;
                    try (PreparedStatement p = c.prepareStatement("UPDATE application.journey SET revision=revision+1,updated_at=now() WHERE journey_id=? AND owner_id=? AND revision=?")) {
                        p.setObject(1, journey); p.setObject(2, owner); p.setLong(3, expected); changed = p.executeUpdate();
                    }
                    if (changed == 0) {
                        try (PreparedStatement p = c.prepareStatement("UPDATE cap60_harness.command_key SET response_status=409 WHERE journey_id=? AND key_hash=?")) { p.setObject(1, journey); p.setBytes(2, key); p.executeUpdate(); }
                        c.commit(); return new Outcome(409, false, currentRevision(journey, owner), "REVISION_CONFLICT");
                    }
                    failIf(2, failAfter);
                    UUID audit = UUID.randomUUID();
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO cap60_harness.audit_event VALUES (?, ?, ?, 'JOURNEY_ADVANCED')")) { p.setObject(1, audit); p.setObject(2, journey); p.setLong(3, expected + 1); p.executeUpdate(); }
                    failIf(3, failAfter);
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO cap60_harness.step_event VALUES (?, ?, ?, 'JOURNEY_ADVANCED')")) { p.setObject(1, UUID.randomUUID()); p.setObject(2, journey); p.setLong(3, expected + 1); p.executeUpdate(); }
                    failIf(4, failAfter);
                    try (PreparedStatement p = c.prepareStatement("UPDATE cap60_harness.command_key SET response_status=201 WHERE journey_id=? AND key_hash=?")) { p.setObject(1, journey); p.setBytes(2, key); p.executeUpdate(); }
                    c.commit(); return new Outcome(201, false, expected + 1, "ADVANCED");
                } catch (RuntimeException | SQLException error) { c.rollback(); throw error; }
            }
        }
        private boolean owned(Connection c, UUID journey, UUID owner) throws SQLException {
            try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM application.journey WHERE journey_id=? AND owner_id=?")) { p.setObject(1, journey); p.setObject(2, owner); try (ResultSet r=p.executeQuery()) { return r.next(); } }
        }
        private long currentRevision(UUID journey, UUID owner) throws SQLException {
            try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement("SELECT revision FROM application.journey WHERE journey_id=? AND owner_id=?")) { p.setObject(1, journey); p.setObject(2, owner); try (ResultSet r=p.executeQuery()) { return r.next() ? r.getLong(1) : 0; } }
        }
        private static void failIf(int write, int failAfter) { if (write == failAfter) throw new InjectedFailure(); }
        static byte[] decodeHash(String value) {
            if (value == null || !value.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("hash must be a lowercase SHA-256 hex value");
            return HexFormat.of().parseHex(value);
        }
    }

    static final class InjectedFailure extends RuntimeException { }

    @Configuration @Profile("cap60-runtime-test")
    static class RuntimeConfiguration {
        @Bean ReferenceAdapter cap60ReferenceAdapter(Environment env) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(env.getRequiredProperty("coverstory.application.jdbc.url"));
            dataSource.setUsername(env.getRequiredProperty("coverstory.application.jdbc.username"));
            dataSource.setPassword(env.getProperty("coverstory.application.jdbc.password", ""));
            return new ReferenceAdapter(dataSource);
        }
        @Bean FilterRegistrationBean<Filter> syntheticActorOnlyForThisInstructorRuntime() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> {
                HttpServletRequest http = (HttpServletRequest) request;
                String actor = "owner-a".equals(http.getHeader("X-CAP60-Test-Actor")) ? OWNER.toString()
                        : "owner-b".equals(http.getHeader("X-CAP60-Test-Actor")) ? OTHER_OWNER.toString() : null;
                HttpServletRequest wrapped = actor == null ? http : new HttpServletRequestWrapper(http) { @Override public Principal getUserPrincipal() { return () -> actor; } };
                chain.doFilter(wrapped, response);
            });
            registration.setUrlPatterns(List.of("/api/__instructor/cap60/*"));
            return registration;
        }
    }
}
