package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** Actual PostgreSQL schema/fixture/oracle checks; they do not implement CAP57Seam. */
class CAP57InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-57/synthetic-v1.json");

    @Test void syntheticInputsHaveMatchingCanonicalContentHashes() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("CAP-57-synthetic-v1", root.path("fixture_id").asText());
        assertEquals(3, root.path("prepared_versions").size());
        assertEquals(4, root.path("cases").size());
        for (JsonNode offer : root.path("prepared_versions")) {
            Map<String, Object> canonical = new TreeMap<>();
            for (String field : List.of("product_id", "version_id", "source_version", "restrictions", "amount_minor", "currency"))
                canonical.put(field, nativeValue(offer.path(field)));
            String content = JSON.writeValueAsString(canonical);
            String actual = java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8)));
            assertEquals(offer.path("content_sha256").asText(), actual, "canonical hash for " + offer.path("version_id").asText());
        }
    }

    @Test void embeddedPostgresLoadsThreePreparedVersionsAndEnforcesImmutableTerms() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            load(s);
            try (ResultSet rows = s.executeQuery("SELECT count(*) FROM catalog.health_offer_version")) {
                assertTrue(rows.next()); assertEquals(3, rows.getInt(1));
            }
            assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE catalog.health_offer_version "
                    + "SET restrictions='{}'::jsonb WHERE version_id='a8100000-0000-4000-8000-000000000001'"));
            assertThrows(SQLException.class, () -> s.executeUpdate("DELETE FROM catalog.health_offer_version "
                    + "WHERE version_id='a8100000-0000-4000-8000-000000000001'"));
            assertEquals(0, count(s, "SELECT count(*) FROM catalog.active_health_offer"));
        }
    }

    @Test void correctAndDefectiveSqlWitnessesProveExportDuplicateConflictAndRollback() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                load(s);
                s.executeUpdate("INSERT INTO catalog.active_health_offer(product_id,version_id) VALUES "
                        + "('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000001')");
                try (ResultSet r = s.executeQuery("SELECT product_id,version_id,content_sha256,amount_minor,currency "
                        + "FROM catalog.published_health_offer_export")) {
                    assertTrue(r.next());
                    assertEquals("a8100000-0000-4000-8000-000000000001", r.getString("version_id"));
                    assertEquals("cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160", r.getString("content_sha256"));
                    assertEquals(123456L, r.getLong("amount_minor")); assertEquals("INR", r.getString("currency").trim());
                    assertFalse(r.next());
                }

                // Positive duplicate witness: same selected version has one active row.
                assertEquals(0, s.executeUpdate("INSERT INTO catalog.active_health_offer(product_id,version_id) VALUES "
                        + "('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000001') "
                        + "ON CONFLICT (product_id) DO NOTHING"));
                assertEquals(1, count(s, "SELECT count(*) FROM catalog.active_health_offer"));

                // Negative source-rewrite witness: changed restrictions cannot mutate the prepared version.
                assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE catalog.health_offer_version "
                        + "SET restrictions='{\"waiting_period_months\":0}'::jsonb "
                        + "WHERE product_id='5f1b8b02-1b68-4de8-928c-f3eaa5f80001' "
                        + "AND version_id='a8100000-0000-4000-8000-000000000001'"));
                assertEquals(1, count(s, "SELECT count(*) FROM catalog.active_health_offer"));
            }

            // Fault after an activation write must roll back the export state.
            try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                s.execute("BEGIN");
                s.executeUpdate("INSERT INTO catalog.active_health_offer(product_id,version_id) VALUES "
                        + "('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000002') "
                        + "ON CONFLICT (product_id) DO UPDATE SET version_id=EXCLUDED.version_id");
                s.execute("ROLLBACK");
                assertEquals("a8100000-0000-4000-8000-000000000001", scalar(s,
                        "SELECT version_id::text FROM catalog.active_health_offer"));
            }
        }
    }

    private static Object nativeValue(JsonNode node) {
        if (node.isObject()) {
            Map<String, Object> sorted = new TreeMap<>(); node.fields().forEachRemaining(e -> sorted.put(e.getKey(), nativeValue(e.getValue()))); return sorted;
        }
        if (node.isArray()) { List<Object> list = new ArrayList<>(); node.forEach(v -> list.add(nativeValue(v))); return list; }
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isIntegralNumber()) return node.asLong();
        if (node.isFloatingPointNumber()) return node.asDouble();
        return null;
    }

    private static void load(Statement s) throws Exception {
        executeResource(s, "/db/schema/ownership.sql");
        executeResource(s, "/db/schema/catalog_publication.sql");
        executeResource(s, "/coverstory/catalog/cap_57_fixtures.sql");
    }

    private static void executeResource(Statement s, String path) throws Exception {
        try (var in = CAP57InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing SQL resource " + path);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            boolean dollar = false, single = false, lineComment = false; StringBuilder statement = new StringBuilder();
            for (int i = 0; i < sql.length(); i++) {
                if (!single && !dollar && sql.startsWith("--", i)) { lineComment = true; }
                if (lineComment) { char commentChar = sql.charAt(i); if (commentChar == '\n') { lineComment = false; statement.append(commentChar); } continue; }
                if (!single && sql.startsWith("$$", i)) { dollar = !dollar; statement.append("$$"); i++; continue; }
                char c = sql.charAt(i);
                if (!dollar && c == '\'') single = !single;
                if (!dollar && !single && c == ';') {
                    if (!statement.toString().isBlank()) s.execute(statement.toString());
                    statement.setLength(0);
                } else statement.append(c);
            }
            if (!statement.toString().isBlank()) s.execute(statement.toString());
        }
    }

    private static int count(Statement s, String sql) throws Exception {
        try (ResultSet r = s.executeQuery(sql)) { assertTrue(r.next()); return r.getInt(1); }
    }

    private static String scalar(Statement s, String sql) throws Exception {
        try (ResultSet r = s.executeQuery(sql)) { assertTrue(r.next()); return r.getString(1); }
    }
}
