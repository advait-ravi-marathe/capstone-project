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

    private static void execute(Statement s, String path) throws Exception {
        try (var in = CAP42InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing fixture resource " + path);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : content.split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
}
