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
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP45SqlAcceptanceTest {
    private static EmbeddedPostgres postgres;
    private static CAP45SqlPortAdapter adapter;
    private static final OffsetDateTime FROM = OffsetDateTime.parse("2026-10-13T00:00:00Z");
    private static final OffsetDateTime TO = OffsetDateTime.parse("2026-10-14T00:00:00Z");

    @BeforeAll static void startDatabase() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        execute("/coverstory/catalog/cap_45_schema.sql");
        execute("/coverstory/catalog/cap_45_fixtures.sql");
        adapter = new CAP45SqlPortAdapter(postgres.getPostgresDatabase());
    }
    @AfterAll static void stopDatabase() throws IOException { if (postgres != null) postgres.close(); }

    @Test void leftJoinPreservesEvidenceOnceAndDoesNotFanOutAmounts() throws Exception {
        List<CAP45SqlPortAdapter.Row> rows = adapter.query("editor-E", FROM, TO);
        assertEquals(List.of("source-A", "source-B", "source-C"), rows.stream().map(CAP45SqlPortAdapter.Row::sourceId).sorted().toList());
        assertEquals(3, rows.size(), "one row per evidence identity despite multiple attempts");
        assertEquals(new BigDecimal("12000"), rows.stream().map(CAP45SqlPortAdapter.Row::amountMinor)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        var noAttempts = find(rows, "source-B");
        if (noAttempts == null) return;
        assertEquals(0, noAttempts.attemptCount()); assertNull(noAttempts.lastAttemptAt()); assertFalse(noAttempts.hasAttempt());
    }

    @Test void compositeOwnerKeyDoesNotBorrowAnotherEditorsAttempts() throws Exception {
        var row = find(adapter.query("editor-E", FROM, TO), "source-B");
        if (row == null) return;
        assertEquals(0, row.attemptCount()); assertNull(row.lastAttemptAt()); assertFalse(row.hasAttempt());
    }

    @Test void nullableAmountStaysNullWhileAttemptsRemainFactual() throws Exception {
        var row = find(adapter.query("editor-E", FROM, TO), "source-C");
        if (row == null) return;
        assertNull(row.amountMinor()); assertEquals(1, row.attemptCount()); assertTrue(row.hasAttempt());
    }

    @Test void attemptAfterCaptureWindowStillBelongsToQualifyingEvidence() throws Exception {
        var row = find(adapter.query("editor-E", FROM, TO), "source-A");
        if (row == null) return;
        assertEquals(2, row.attemptCount()); assertEquals(OffsetDateTime.parse("2026-10-15T01:00:00Z"), row.lastAttemptAt());
        assertEquals(10000, row.amountMinor().longValueExact());
    }

    private static void execute(String path) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            for (String sql : resource(path).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private static CAP45SqlPortAdapter.Row find(List<CAP45SqlPortAdapter.Row> rows, String sourceId) {
        CAP45SqlPortAdapter.Row row = rows.stream().filter(item -> item.sourceId().equals(sourceId)).findFirst().orElse(null);
        assertNotNull(row, "missing evidence row " + sourceId + " (expected when learner SQL remains the empty TODO)");
        return row;
    }
    private static String resource(String path) throws IOException {
        try (var in = CAP45SqlAcceptanceTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing SQL fixture " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
