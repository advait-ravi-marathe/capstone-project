package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Packaging and runtime boundary smoke for the built executable JAR (not learner feature acceptance). */
class LocalPackagedRuntimeInfrastructureTest {
    @Test
    void packagedJarServesRootHealthAndEnforcesAnonymousAndCsrfBoundaries() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess runtime = LocalPackagedRuntimeProcess.start(postgres)) {
            assertEquals(3, policyCount(postgres.getPostgresDatabase()), "packaged startup must apply V001–V003");
            var root = runtime.get("/");
            System.out.println("packaged-root=" + root.statusCode() + " body=" + root.body());
            assertEquals(200, root.statusCode(), root.body());
            assertTrue(root.body().contains("CoverStory Local Training"));
            assertEquals(200, runtime.get("/actuator/health").statusCode());
            assertEquals(401, runtime.get("/api/session").statusCode());
            assertEquals(401, runtime.get("/api/policies/policy-demo-1").statusCode());
            var csrf = runtime.get("/api/csrf");
            assertEquals(200, csrf.statusCode());
            String csrfCookie = cookie(csrf, "XSRF-TOKEN");
            String token = csrfCookie.substring("XSRF-TOKEN=".length());
            var selected = runtime.post("/api/session", "{\"ownerId\":\"owner-a\"}",
                    csrfCookie, null, token);
            assertEquals(200, selected.statusCode(), selected.body());
            String selectedSession = cookie(selected, "JSESSIONID");
            assertEquals(403, runtime.post("/api/policies", "{}", csrfCookie, selectedSession, null).statusCode());
            assertEquals(404, runtime.get("/test/day28", selectedSession).statusCode());
            assertEquals(404, runtime.get("/__e2e/day45-renewal-preview", selectedSession).statusCode());
        }
    }

    private static long policyCount(javax.sql.DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            if (!rows.next()) throw new IllegalStateException("count query returned no row");
            return rows.getLong(1);
        }
    }

    private static String cookie(java.net.http.HttpResponse<?> response, String name) {
        return response.headers().allValues("set-cookie").stream()
                .filter(value -> value.startsWith(name + "="))
                .map(value -> value.split(";", 2)[0])
                .findFirst().orElseThrow(() -> new AssertionError("Missing " + name + " cookie"));
    }
}
