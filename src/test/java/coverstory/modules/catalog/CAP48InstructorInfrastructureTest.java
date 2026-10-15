package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Proves the independent PostgreSQL/date-spine fixtures load before learner SQL. */
class CAP48InstructorInfrastructureTest {
    @Test void publicFixtureOracleCoversUtcSpineNullsAndTimezoneControl() throws Exception {
        JsonNode fixture = new tools.jackson.databind.ObjectMapper().readTree(
                Files.readString(Path.of("fixtures/coverstory/CAP-48/synthetic-v1.json")));
        assertEquals("CAP-48-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals(3, fixture.path("date_spine").size());
        assertEquals(2, fixture.path("expected").get(1).path("published_count").asInt());
        assertEquals(2, fixture.path("session_timezones").size());
    }

    @Test void loadsSyntheticRowsAndExplicitUtcDateSpine() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_48_schema.sql"); execute(s, "/coverstory/catalog/cap_48_fixtures.sql");
            try (ResultSet r = s.executeQuery("SELECT (SELECT count(*) FROM catalog_date_spine), (SELECT count(*) FROM catalog_import_evidence), (SELECT count(*) FROM catalog_import_evidence WHERE base_amount_minor IS NULL)")) {
                assertTrue(r.next()); assertEquals(3, r.getInt(1)); assertEquals(5, r.getInt(2)); assertEquals(1, r.getInt(3));
            }
        }
    }

    @Test void independentUtcDateSpineOracleRejectsObservedOnlyTimezoneAndOwnerMutants() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            execute(s, "/coverstory/catalog/cap_48_schema.sql");
            execute(s, "/coverstory/catalog/cap_48_fixtures.sql");
            List<Daily> expected = List.of(new Daily("2026-10-13", 1, new BigDecimal("1000")),
                    new Daily("2026-10-14", 2, new BigDecimal("500")),
                    new Daily("2026-10-15", 0, BigDecimal.ZERO));
            for (String zone : List.of("UTC", "Asia/Kolkata")) {
                try (Statement setZone = c.createStatement()) { setZone.execute("SET TIME ZONE '" + zone + "'"); }
                assertEquals(expected, dateSpineReference(c));
            }

            try (Statement setZone = c.createStatement()) { setZone.execute("SET TIME ZONE 'Asia/Kolkata'"); }
            assertEquals(2, observedOnlyMutant(c).size(), "observed-only grouping must omit the explicit empty day");
            List<Daily> localCast = localSessionDateCastMutant(c);
            assertNotEquals(expected, localCast, "session-local timestamptz::date must be detected");
            List<Daily> noOwner = dateSpineWithoutOwnerFilter(c);
            assertEquals(3, noOwner.get(1).count());
            assertEquals(new BigDecimal("90500"), noOwner.get(1).amount());
            assertNotEquals(expected, noOwner, "wrong-editor aggregate must be detected");
        }
    }

    private static List<Daily> dateSpineReference(Connection c) throws Exception {
        return daily(c, "SELECT d.day_utc::text, count(e.source_id)::bigint, "
                + "coalesce(sum(e.base_amount_minor),0)::numeric FROM catalog_date_spine d "
                + "LEFT JOIN catalog_import_evidence e ON e.editor_fixture=? AND e.publication_state='PUBLISHED' "
                + "AND (e.captured_at AT TIME ZONE 'UTC')::date=d.day_utc "
                + "GROUP BY d.day_utc ORDER BY d.day_utc", "editor-E");
    }

    private static List<Daily> observedOnlyMutant(Connection c) throws Exception {
        return daily(c, "SELECT (e.captured_at AT TIME ZONE 'UTC')::date::text,count(*),"
                + "coalesce(sum(e.base_amount_minor),0)::numeric FROM catalog_import_evidence e "
                + "WHERE e.editor_fixture=? AND e.publication_state='PUBLISHED' "
                + "GROUP BY (e.captured_at AT TIME ZONE 'UTC')::date ORDER BY 1", "editor-E");
    }

    private static List<Daily> localSessionDateCastMutant(Connection c) throws Exception {
        return daily(c, "SELECT d.day_utc::text,count(e.source_id)::bigint,coalesce(sum(e.base_amount_minor),0)::numeric "
                + "FROM catalog_date_spine d LEFT JOIN catalog_import_evidence e ON e.editor_fixture=? "
                + "AND e.publication_state='PUBLISHED' AND e.captured_at::date=d.day_utc "
                + "GROUP BY d.day_utc ORDER BY d.day_utc", "editor-E");
    }

    private static List<Daily> dateSpineWithoutOwnerFilter(Connection c) throws Exception {
        return daily(c, "SELECT d.day_utc::text,count(e.source_id)::bigint,coalesce(sum(e.base_amount_minor),0)::numeric "
                + "FROM catalog_date_spine d LEFT JOIN catalog_import_evidence e ON e.publication_state='PUBLISHED' "
                + "AND (e.captured_at AT TIME ZONE 'UTC')::date=d.day_utc "
                + "GROUP BY d.day_utc ORDER BY d.day_utc", null);
    }

    private static List<Daily> daily(Connection c, String sql, String editor) throws Exception {
        try (var p = c.prepareStatement(sql)) {
            if (editor != null) p.setString(1, editor);
            try (ResultSet r = p.executeQuery()) {
                List<Daily> rows = new ArrayList<>();
                while (r.next()) rows.add(new Daily(r.getString(1), r.getLong(2), r.getBigDecimal(3)));
                return rows;
            }
        }
    }

    private record Daily(String day, long count, BigDecimal amount) { }
    private static void execute(Statement s, String path) throws Exception {
        try (var in = CAP48InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in); for (String sql : new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
}
