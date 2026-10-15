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
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP51SqlAcceptanceTest {
    private static EmbeddedPostgres postgres;
    private static final OffsetDateTime FROM = OffsetDateTime.parse("2026-10-13T00:00:00Z");
    private static final OffsetDateTime TO = OffsetDateTime.parse("2026-10-14T00:00:00Z");

    @BeforeAll static void startDatabase() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        execute("/coverstory/catalog/cap_51_schema.sql");
        execute("/coverstory/catalog/cap_51_fixtures.sql");
    }
    @AfterAll static void stopDatabase() throws IOException { if (postgres != null) postgres.close(); }

    @Test void countsLogicalEvidenceAndDoesNotMultiplyItsAmountForRetries() throws Exception {
        assertEquals(new Summary(2, new BigDecimal("12000"), 1), query("editor-E", FROM, TO));
    }

    @Test void nullableAmountCountsAsEvidenceButIsNotAPrice() throws Exception {
        var from = OffsetDateTime.parse("2026-10-14T14:00:00Z");
        var to = OffsetDateTime.parse("2026-10-14T15:00:00Z");
        assertEquals(new Summary(1, BigDecimal.ZERO, 0), query("editor-E", from, to));
        try (Connection c = postgres.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT base_amount_minor FROM catalog_import_evidence WHERE editor_fixture='editor-E' AND source_id='source-C'")) {
            try (ResultSet rs = p.executeQuery()) { assertTrue(rs.next()); assertNull(rs.getObject(1)); }
        }
    }

    @Test void successfulAttemptFromAnotherEditorIsNotBorrowed() throws Exception {
        var from = OffsetDateTime.parse("2026-10-12T12:00:00Z");
        var to = OffsetDateTime.parse("2026-10-12T13:00:00Z");
        assertEquals(new Summary(1, new BigDecimal("300"), 0), query("editor-E", from, to));
    }

    @Test void anotherSuccessfulRetryLeavesPerEvidenceSummaryUnchanged() throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO catalog_import_attempt VALUES ('editor-E','source-A','attempt-3',true,'2026-10-13T10:03:00Z')")) {
            assertEquals(1, p.executeUpdate());
        }
        assertEquals(new Summary(2, new BigDecimal("12000"), 1), query("editor-E", FROM, TO));
    }

    private static Summary query(String editor, OffsetDateTime from, OffsetDateTime to) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection();
             PreparedStatement p = c.prepareStatement(resource("/coverstory/catalog/cap_51.sql"))) {
            p.setString(1, editor); p.setObject(2, from); p.setObject(3, to);
            try (ResultSet rs = p.executeQuery()) {
                assertTrue(rs.next(), "learner query returns one aggregate summary row");
                Summary value = new Summary(rs.getLong("published_count"), rs.getBigDecimal("known_base_total_minor"),
                        rs.getLong("confirmed_import_count"));
                assertFalse(rs.next());
                return value;
            }
        }
    }

    private static void execute(String path) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            for (String sql : resource(path).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private static String resource(String path) throws IOException {
        try (var in = CAP51SqlAcceptanceTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing SQL resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private record Summary(long publishedCount, BigDecimal total, long confirmedCount) { }
}
