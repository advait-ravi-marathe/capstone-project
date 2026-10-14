package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned fixture/reference checks do not invoke the learner SQL TODO. */
class CAP45InstructorInfrastructureTest {
    private static final String FROM = "2026-10-13T00:00:00Z";
    private static final String TO = "2026-10-14T00:00:00Z";

    @Test void fixtureHasTheFourSavedJoinCases() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(Path.of("fixtures/coverstory/CAP-45/synthetic-v1.json")));
        assertEquals("CAP-45-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals("SYNTHETIC", fixture.path("classification").asText());
        assertEquals(4, fixture.path("cases").size());
        assertTrue(fixture.path("attempts_after_capture_window").asBoolean());
    }

    @Test void embeddedPostgresIndependentReferenceAndMutantsCheckCardinalityOwnershipAndNoFanout() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_45_schema.sql");
            execute(s, "/coverstory/catalog/cap_45_fixtures.sql");

            List<Row> expected = run(c, "WITH attempt_rollup AS ("
                    + " SELECT editor_fixture,source_id,source_version,count(*)::int attempt_count,max(attempted_at) last_attempt_at "
                    + " FROM catalog_import_attempt GROUP BY editor_fixture,source_id,source_version) "
                    + "SELECT e.source_id,e.base_amount_minor,coalesce(a.attempt_count,0) attempt_count,a.last_attempt_at "
                    + "FROM catalog_import_evidence e LEFT JOIN attempt_rollup a "
                    + "ON a.editor_fixture=e.editor_fixture AND a.source_id=e.source_id AND a.source_version=e.source_version "
                    + "WHERE e.editor_fixture=? AND e.publication_state='PUBLISHED' AND e.captured_at>=?::timestamptz AND e.captured_at<?::timestamptz "
                    + "ORDER BY e.source_id", "editor-E", FROM, TO);
            assertEquals(List.of("source-A", "source-B", "source-C"), expected.stream().map(Row::source).toList());
            assertEquals(2, expected.stream().filter(row -> row.source().equals("source-A")).findFirst().orElseThrow().attempts());
            assertEquals(0, expected.stream().filter(row -> row.source().equals("source-B")).findFirst().orElseThrow().attempts());
            assertNull(expected.stream().filter(row -> row.source().equals("source-B")).findFirst().orElseThrow().lastAttempt());
            assertNull(expected.stream().filter(row -> row.source().equals("source-C")).findFirst().orElseThrow().amount());
            assertEquals(1, expected.stream().filter(row -> row.source().equals("source-C")).findFirst().orElseThrow().attempts());
            assertEquals(12000, expected.stream().map(Row::amount).filter(java.util.Objects::nonNull).mapToLong(Long::longValue).sum());

            List<Row> innerJoinMutant = run(c, "SELECT e.source_id,e.base_amount_minor,count(a.attempt_number)::int attempt_count,max(a.attempted_at) last_attempt_at "
                    + "FROM catalog_import_evidence e JOIN catalog_import_attempt a "
                    + "ON a.editor_fixture=e.editor_fixture AND a.source_id=e.source_id AND a.source_version=e.source_version "
                    + "WHERE e.editor_fixture=? AND e.publication_state='PUBLISHED' AND e.captured_at>=?::timestamptz AND e.captured_at<?::timestamptz "
                    + "GROUP BY e.source_id,e.base_amount_minor ORDER BY e.source_id", "editor-E", FROM, TO);
            assertEquals(2, innerJoinMutant.size(), "INNER JOIN mutant must lose the no-attempt evidence row");

            try (var p = c.prepareStatement("SELECT count(*) FROM catalog_import_evidence e JOIN catalog_import_attempt a "
                    + "ON a.source_id=e.source_id AND a.source_version=e.source_version "
                    + "WHERE e.editor_fixture=? AND e.source_id='source-B' AND e.publication_state='PUBLISHED'")) {
                p.setString(1, "editor-E");
                try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); assertEquals(1, r.getInt(1), "source-only owner mutant borrows editor-F attempt"); }
            }

            try (var p = c.prepareStatement("SELECT sum(e.base_amount_minor) FROM catalog_import_evidence e JOIN catalog_import_attempt a "
                    + "ON a.editor_fixture=e.editor_fixture AND a.source_id=e.source_id AND a.source_version=e.source_version "
                    + "WHERE e.editor_fixture=? AND e.publication_state='PUBLISHED' AND e.captured_at>=?::timestamptz AND e.captured_at<?::timestamptz")) {
                p.setString(1, "editor-E"); p.setString(2, FROM); p.setString(3, TO);
                try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); assertEquals(20000L, r.getLong(1), "raw one-to-many join fans out source-A amount"); }
            }
        }
    }

    private static List<Row> run(Connection c, String sql, String editor, String from, String to) throws Exception {
        try (var p = c.prepareStatement(sql)) {
            p.setString(1, editor); p.setString(2, from); p.setString(3, to);
            try (ResultSet r = p.executeQuery()) {
                List<Row> rows = new ArrayList<>();
                while (r.next()) {
                    long amount = r.getLong("base_amount_minor");
                    Long nullableAmount = r.wasNull() ? null : amount;
                    rows.add(new Row(r.getString("source_id"), nullableAmount,
                            r.getInt("attempt_count"), r.getObject("last_attempt_at", OffsetDateTime.class)));
                }
                return rows;
            }
        }
    }

    private static void execute(Statement s, String resource) throws Exception {
        try (var in = CAP45InstructorInfrastructureTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing fixture " + resource);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) if (!statement.isBlank()) s.execute(statement);
        }
    }

    private record Row(String source, Long amount, int attempts, OffsetDateTime lastAttempt) { }
}
