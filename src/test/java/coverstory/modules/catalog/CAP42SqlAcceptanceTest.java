package coverstory.modules.catalog;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP42SqlAcceptanceTest {
    private static EmbeddedPostgres postgres;

    @BeforeAll static void startAndLoadSyntheticDatabase() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        execute("/coverstory/catalog/cap_42_schema.sql");
        execute("/coverstory/catalog/cap_42_fixtures.sql");
    }
    @AfterAll static void stop() throws IOException { if (postgres != null) postgres.close(); }

    @Test void exactEditorAndHalfOpenBoundsExcludeDraftAndOtherEditor() throws Exception {
        var result = query("editor-E", "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z");
        assertEquals(2, result.count); assertEquals(0, new BigDecimal("12000").compareTo(result.amount));
    }
    @Test void emptyWindowReturnsZeroAggregateRow() throws Exception {
        var result = query("editor-Z", "2026-10-13T00:00:00Z", "2026-10-14T00:00:00Z");
        assertEquals(0, result.count); assertEquals(0, BigDecimal.ZERO.compareTo(result.amount));
    }
    @Test void numericSumDoesNotOverflowIntegerOrLosePrecision() throws Exception {
        var result = query("editor-E", "2026-10-13T14:00:00Z", "2026-10-13T16:00:00Z");
        assertEquals(2, result.count); assertEquals(0, new BigDecimal("10000000001").compareTo(result.amount));
    }
    @Test void schemaRejectsMixedCurrencyInsteadOfSummingIt() throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO catalog_import_evidence VALUES ('editor-E','bad','PUBLISHED',100,'USD',TIMESTAMPTZ '2026-10-13T12:00:00Z')")) {
            assertThrows(java.sql.SQLException.class, p::executeUpdate);
        }
    }

    private static Result query(String editor, String from, String to) throws Exception {
        String sql = resource("/coverstory/catalog/cap_42.sql");
        try (Connection c = postgres.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, editor); p.setObject(2, java.time.OffsetDateTime.parse(from)); p.setObject(3, java.time.OffsetDateTime.parse(to));
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next(), "query returns exactly one aggregate row");
                Result result = new Result(r.getLong("published_count"), r.getBigDecimal("known_base_total_minor"));
                assertFalse(r.next()); return result;
            }
        }
    }
    private static void execute(String resource) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            for (String sql : resource(resource).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private static String resource(String name) throws IOException {
        try (var in = CAP42SqlAcceptanceTest.class.getResourceAsStream(name)) {
            if (in == null) throw new IOException("Missing instructor-owned SQL fixture: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private record Result(long count, BigDecimal amount) { }
}
