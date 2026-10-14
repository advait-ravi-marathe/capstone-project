package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Actual PostgreSQL fixture/schema smoke; it does not run the learner SQL. */
class CAP42InstructorInfrastructureTest {
    @Test void publicFixtureOracleHasAllFourSavedContractCases() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(Path.of("fixtures/coverstory/CAP-42/synthetic-v1.json")));
        assertEquals("CAP-42-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals(4, fixture.path("cases").size());
        assertEquals(10000000001L, fixture.path("numeric_boundary").path("expected_total").asLong());
    }

    @Test void startsPostgresLoadsOnlySyntheticFixtureAndEnforcesCurrencyBoundary() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_42_schema.sql");
            execute(s, "/coverstory/catalog/cap_42_fixtures.sql");
            try (ResultSet r = s.executeQuery("SELECT count(*) FROM catalog_import_evidence")) {
                assertTrue(r.next()); assertEquals(7, r.getInt(1));
            }
            assertThrows(java.sql.SQLException.class, () -> s.executeUpdate(
                    "INSERT INTO catalog_import_evidence VALUES ('E','bad','PUBLISHED',1,'USD',now())"));
        }
    }

    @Test void independentReferenceAndMutantsExerciseOwnershipBoundsAndExactNumericTotals() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_42_schema.sql");
            execute(s, "/coverstory/catalog/cap_42_fixtures.sql");
            assertEquals(new Expected(2, new BigDecimal("12000")), reference(c, "editor-E",
                    "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z"));
            assertEquals(new Expected(0, BigDecimal.ZERO), reference(c, "editor-Z",
                    "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z"));
            assertEquals(new Expected(2, new BigDecimal("10000000001")), reference(c, "editor-E",
                    "2026-10-12T14:00:00Z", "2026-10-12T16:00:00Z"));

            assertNotEquals(new Expected(2, new BigDecimal("12000")), referenceIgnoringEditor(c,
                    "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z"), "wrong-editor mutant must be caught");
            assertNotEquals(new Expected(2, new BigDecimal("12000")), referenceIncludingUpperBound(c,
                    "editor-E", "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z"), "inclusive upper-bound mutant must be caught");
        }
    }

    private static Expected reference(Connection c, String editor, String from, String to) throws Exception {
        try (PreparedStatement p = c.prepareStatement("SELECT count(*) AS n, coalesce(sum(base_amount_minor),0)::numeric AS total "
                + "FROM catalog_import_evidence WHERE editor_fixture=? AND publication_state='PUBLISHED' "
                + "AND currency='INR' AND captured_at>=?::timestamptz AND captured_at<?::timestamptz")) {
            p.setString(1, editor); p.setString(2, from); p.setString(3, to);
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return new Expected(r.getLong(1), r.getBigDecimal(2)); }
        }
    }

    private static Expected referenceIgnoringEditor(Connection c, String from, String to) throws Exception {
        try (PreparedStatement p = c.prepareStatement("SELECT count(*), coalesce(sum(base_amount_minor),0)::numeric "
                + "FROM catalog_import_evidence WHERE publication_state='PUBLISHED' AND currency='INR' "
                + "AND captured_at>=?::timestamptz AND captured_at<?::timestamptz")) {
            p.setString(1, from); p.setString(2, to);
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return new Expected(r.getLong(1), r.getBigDecimal(2)); }
        }
    }

    private static Expected referenceIncludingUpperBound(Connection c, String editor, String from, String to) throws Exception {
        try (PreparedStatement p = c.prepareStatement("SELECT count(*), coalesce(sum(base_amount_minor),0)::numeric "
                + "FROM catalog_import_evidence WHERE editor_fixture=? AND publication_state='PUBLISHED' AND currency='INR' "
                + "AND captured_at>=?::timestamptz AND captured_at<=?::timestamptz")) {
            p.setString(1, editor); p.setString(2, from); p.setString(3, to);
            try (ResultSet r = p.executeQuery()) { assertTrue(r.next()); return new Expected(r.getLong(1), r.getBigDecimal(2)); }
        }
    }

    private record Expected(long count, BigDecimal total) { }

    private static void execute(Statement s, String path) throws Exception {
        try (var in = CAP42InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing fixture resource " + path);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : content.split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
}
