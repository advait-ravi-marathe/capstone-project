package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Day16PaginationReferenceTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    record Page(List<String> ids, int total) { }

    @BeforeAll static void startDisposablePostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE teaching_notice (board_id TEXT NOT NULL, id TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL, PRIMARY KEY(board_id,id))");
        }
    }

    @AfterAll static void closeDisposablePostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach void seedTiedTimestamps() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("TRUNCATE teaching_notice");
        }
        insert("N2", "2026-01-01T10:00:00Z");
        insert("N1", "2026-01-01T10:00:00Z");
        insert("N4", "2026-01-01T09:00:00Z");
        insert("N3", "2026-01-01T09:00:00Z");
        insert("N5", "2026-01-01T08:00:00Z");
    }

    @Test void equalTimestampsUseAscendingIdAndPagesHaveExactBoundaries() throws Exception {
        assertEquals(new Page(List.of("N1", "N2"), 5), page("B", 0, 2));
        assertEquals(new Page(List.of("N3", "N4"), 5), page("B", 1, 2));
        assertEquals(new Page(List.of("N5"), 5), page("B", 2, 2));
        assertEquals(new Page(List.of(), 5), page("B", 3, 2));
        var adjacent = new ArrayList<String>();
        adjacent.addAll(page("B", 0, 2).ids());
        adjacent.addAll(page("B", 1, 2).ids());
        assertEquals(List.of("N1", "N2", "N3", "N4"), adjacent);
    }

    @Test void boundsAreRejectedAndSizeIsCappedAtTwenty() {
        assertThrows(IllegalArgumentException.class, () -> page("B", -1, 2));
        assertThrows(IllegalArgumentException.class, () -> page("B", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> page("B", 0, 21));
    }

    @Test void insertionBetweenOffsetRequestsCanRepeatAnAlreadySeenRow() throws Exception {
        var first = page("B", 0, 2).ids();
        insert("N0", "2026-01-01T10:00:00Z");
        var secondAfterInsert = page("B", 1, 2).ids();
        assertEquals(List.of("N1", "N2"), first);
        assertEquals(List.of("N2", "N3"), secondAfterInsert);
    }

    private static void insert(String id, String timestamp) throws Exception {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("INSERT INTO teaching_notice(board_id,id,created_at) VALUES (?,?,?)")) {
            p.setString(1, "B");
            p.setString(2, id);
            p.setObject(3, Instant.parse(timestamp).atOffset(java.time.ZoneOffset.UTC));
            p.executeUpdate();
        }
    }

    private static Page page(String boardId, int page, int size) {
        if (page < 0 || size < 1 || size > 20) throw new IllegalArgumentException("invalid teaching page");
        long offset = Math.multiplyExact((long) page, size);
        try (Connection c = dataSource.getConnection();
             PreparedStatement count = c.prepareStatement("SELECT count(*) FROM teaching_notice WHERE board_id = ?")) {
            count.setString(1, boardId);
            int total;
            try (ResultSet r = count.executeQuery()) { r.next(); total = r.getInt(1); }
            try (PreparedStatement p = c.prepareStatement("SELECT id FROM teaching_notice WHERE board_id = ? ORDER BY created_at DESC, id ASC LIMIT ? OFFSET ?")) {
                p.setString(1, boardId);
                p.setInt(2, size);
                p.setLong(3, offset);
                try (ResultSet r = p.executeQuery()) {
                    List<String> ids = new ArrayList<>();
                    while (r.next()) ids.add(r.getString(1));
                    return new Page(List.copyOf(ids), total);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("teaching query failed", e);
        }
    }
}
