package coverstory.modules.catalog;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP48SqlAcceptanceTest {
    private static EmbeddedPostgres postgres;
    @BeforeAll static void prepare() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        execute("/coverstory/catalog/cap_48_schema.sql"); execute("/coverstory/catalog/cap_48_fixtures.sql");
    }
    @AfterAll static void stop() throws IOException { if (postgres != null) postgres.close(); }

    @Test void returnsUtcBucketsAndIncludesEmptyDateSpineRows() throws Exception {
        List<Row> rows = query("UTC");
        assertEquals(List.of(
                new Row(LocalDate.parse("2026-10-13"), 1, 1000),
                new Row(LocalDate.parse("2026-10-14"), 2, 500),
                new Row(LocalDate.parse("2026-10-15"), 0, 0)), rows);
    }

    @Test void sessionTimezoneDoesNotMoveEventsAcrossUtcDateBuckets() throws Exception {
        List<Row> utc = query("UTC"); List<Row> kolkata = query("Asia/Kolkata");
        assertEquals(3, utc.size(), "learner query must emit the date spine");
        assertEquals(3, kolkata.size(), "learner query must emit the date spine");
        assertEquals(utc, kolkata);
    }

    @Test void nullAmountsCountButAreNotPricedAndOtherEditorIsExcluded() throws Exception {
        List<Row> rows = query("UTC"); assertEquals(3, rows.size(), "expected one row per date-spine entry");
        Row row = rows.get(1);
        assertEquals(2, row.count); assertEquals(500, row.totalMinor);
    }

    @Test void queryHasStableColumnsAndExactlyOneRowPerDate() throws Exception {
        assertEquals(3, query("UTC").size());
    }

    private static List<Row> query(String timezone) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            s.execute("SET TIME ZONE '" + timezone + "'");
            String query = resource("/coverstory/catalog/cap_48.sql");
            try (ResultSet rs = s.executeQuery(query)) {
                List<Row> rows = new ArrayList<>();
                while (rs.next()) rows.add(new Row(rs.getObject("day_utc", LocalDate.class), rs.getLong("published_count"), rs.getBigDecimal("known_base_total_minor").longValueExact()));
                return rows;
            }
        }
    }
    private static void execute(String path) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            for (String sql : resource(path).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private static String resource(String path) throws IOException {
        try (var in = CAP48SqlAcceptanceTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing instructor-owned SQL fixture: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private record Row(LocalDate day, long count, long totalMinor) { }
}
