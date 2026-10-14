package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/** Proves the independent PostgreSQL/date-spine fixtures load before learner SQL. */
class CAP48InstructorInfrastructureTest {
    @Test void publicFixtureOracleCoversUtcSpineNullsAndTimezoneControl() throws Exception {
        JsonNode fixture = new com.fasterxml.jackson.databind.ObjectMapper().readTree(
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
    private static void execute(Statement s, String path) throws Exception {
        try (var in = CAP48InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in); for (String sql : new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
}
