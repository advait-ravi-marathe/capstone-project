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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day15JoinProjectionReferenceTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    record Spotlight(String isbn, String title) { }
    record Hold(String bookId, int position) { }
    record ShelfSummary(String shelfId, Spotlight spotlight, List<Hold> holds) { }

    @BeforeAll static void startDisposablePostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE teaching_shelf (id TEXT PRIMARY KEY, owner_id TEXT NOT NULL, private_note TEXT NOT NULL)");
            s.execute("CREATE TABLE teaching_spotlight (shelf_id TEXT PRIMARY KEY REFERENCES teaching_shelf(id), isbn TEXT NOT NULL, title TEXT NOT NULL)");
            s.execute("CREATE TABLE teaching_hold (shelf_id TEXT NOT NULL REFERENCES teaching_shelf(id), book_id TEXT NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(shelf_id, book_id))");
        }
    }

    @AfterAll static void closeDisposablePostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach void seedFixedTeachingRows() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("TRUNCATE teaching_hold, teaching_spotlight, teaching_shelf");
            s.execute("INSERT INTO teaching_shelf VALUES ('S1','M1','must not be projected'), ('S2','M1','private'), ('S3','M2','foreign private')");
            s.execute("INSERT INTO teaching_spotlight VALUES ('S2','ISBN7','Rivers')");
            s.execute("INSERT INTO teaching_hold VALUES ('S2','B8',2), ('S2','B3',1)");
        }
    }

    @Test void ownedParentKeepsNullOptionalChildAndEmptyCollectionDistinctFromAbsentParent() throws Exception {
        assertEquals(Optional.of(new ShelfSummary("S1", null, List.of())), readSummary("S1", "M1"));
        assertEquals(Optional.empty(), readSummary("S9", "M1"));
        assertEquals(Optional.empty(), readSummary("S3", "M1"));
    }

    @Test void populatedProjectionContainsOnlyAllowlistedChildFieldsInOrder() throws Exception {
        assertEquals(Optional.of(new ShelfSummary("S2", new Spotlight("ISBN7", "Rivers"),
                List.of(new Hold("B3", 1), new Hold("B8", 2)))), readSummary("S2", "M1"));
    }

    @Test void distinctVariationAllowsOneHoldWithoutOptionalSpotlight() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("INSERT INTO teaching_shelf VALUES ('S4','M1','never selected')");
            s.execute("INSERT INTO teaching_hold VALUES ('S4','B5',1)");
        }
        assertEquals(Optional.of(new ShelfSummary("S4", null, List.of(new Hold("B5", 1)))), readSummary("S4", "M1"));
    }

    private static Optional<ShelfSummary> readSummary(String shelfId, String callerId) throws Exception {
        Spotlight spotlight;
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT s.id, p.isbn, p.title FROM teaching_shelf s LEFT JOIN teaching_spotlight p ON p.shelf_id = s.id WHERE s.id = ? AND s.owner_id = ?")) {
            p.setString(1, shelfId);
            p.setString(2, callerId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                String isbn = r.getString("isbn");
                spotlight = isbn == null ? null : new Spotlight(isbn, r.getString("title"));
            }
        }
        List<Hold> holds = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT book_id, position FROM teaching_hold WHERE shelf_id = ? ORDER BY position ASC")) {
            p.setString(1, shelfId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) holds.add(new Hold(r.getString("book_id"), r.getInt("position")));
            }
        }
        return Optional.of(new ShelfSummary(shelfId, spotlight, List.copyOf(holds)));
    }
}
