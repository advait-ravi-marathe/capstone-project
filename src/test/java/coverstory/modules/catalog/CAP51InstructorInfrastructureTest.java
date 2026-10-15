package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/** Independent SQL reference and known-bad controls; never executes learner SQL. */
class CAP51InstructorInfrastructureTest {
    @Test void savedFixtureHasFourSyntheticOutcomes() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(Path.of("fixtures/coverstory/CAP-51/synthetic-v1.json")));
        assertEquals("CAP-51-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals(4, fixture.path("cases").size());
        assertEquals(12000, fixture.at("/cases/0/expected/known_base_total_minor").asInt());
    }

    @Test void independentExistsReferenceRejectsRawJoinAndCrossEditorMutants() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_51_schema.sql");
            execute(s, "/coverstory/catalog/cap_51_fixtures.sql");
            assertEquals(new Summary(2, new BigDecimal("12000"), 1), reference(c, "editor-E",
                    "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z"));
            assertEquals(new Summary(1, BigDecimal.ZERO, 0), reference(c, "editor-E",
                    "2026-10-14T14:00:00Z", "2026-10-14T15:00:00Z"));
            assertEquals(new Summary(1, new BigDecimal("300"), 0), reference(c, "editor-E",
                    "2026-10-12T12:00:00Z", "2026-10-12T13:00:00Z"));

            Summary rawJoin = rawJoinMutant(c);
            assertEquals(new Summary(3, new BigDecimal("22000"), 2), rawJoin,
                    "joining attempts before aggregation multiplies source-A's amount and confirmed count");
            assertNotEquals(new Summary(2, new BigDecimal("12000"), 1), rawJoin);
            assertEquals(1, crossEditorAttemptMutant(c), "source-only join would borrow editor-F's success");
        }
    }

    private static Summary reference(Connection c, String editor, String from, String to) throws Exception {
        try (var p = c.prepareStatement("SELECT count(*)::bigint,coalesce(sum(e.base_amount_minor),0)::numeric,"
                + "count(*) FILTER (WHERE EXISTS (SELECT 1 FROM catalog_import_attempt a "
                + "WHERE a.editor_fixture=e.editor_fixture AND a.source_id=e.source_id AND a.succeeded))::bigint "
                + "FROM catalog_import_evidence e WHERE e.editor_fixture=? AND e.publication_state='PUBLISHED' "
                + "AND e.captured_at>=?::timestamptz AND e.captured_at<?::timestamptz")) {
            p.setString(1, editor); p.setString(2, from); p.setString(3, to);
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return new Summary(r.getLong(1), r.getBigDecimal(2), r.getLong(3)); }
        }
    }

    private static Summary rawJoinMutant(Connection c) throws Exception {
        try (var p = c.prepareStatement("SELECT count(e.source_id)::bigint,coalesce(sum(e.base_amount_minor),0)::numeric,"
                + "count(a.attempt_id)::bigint FROM catalog_import_evidence e LEFT JOIN catalog_import_attempt a "
                + "ON a.editor_fixture=e.editor_fixture AND a.source_id=e.source_id AND a.succeeded "
                + "WHERE e.editor_fixture='editor-E' AND e.publication_state='PUBLISHED' "
                + "AND e.captured_at>='2026-10-13T00:00:00Z'::timestamptz AND e.captured_at<'2026-10-14T00:00:00Z'::timestamptz")) {
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return new Summary(r.getLong(1), r.getBigDecimal(2), r.getLong(3)); }
        }
    }

    private static int crossEditorAttemptMutant(Connection c) throws Exception {
        try (var p = c.prepareStatement("SELECT count(a.attempt_id) FROM catalog_import_evidence e "
                + "JOIN catalog_import_attempt a ON a.source_id=e.source_id AND a.succeeded "
                + "WHERE e.editor_fixture='editor-E' AND e.source_id='scope-only'")) {
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return r.getInt(1); }
        }
    }

    private static void execute(Statement s, String path) throws Exception {
        try (var in = CAP51InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing fixture " + path);
            for (String sql : new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private record Summary(long count, BigDecimal amount, long confirmed) { }
}
