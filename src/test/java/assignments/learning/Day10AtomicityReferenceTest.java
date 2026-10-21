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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Day10AtomicityReferenceTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll static void startDisposablePostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start(); dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE teaching_names (name TEXT PRIMARY KEY)");
            s.execute("INSERT INTO teaching_names VALUES ('existing')");
        }
    }

    @AfterAll static void closeDisposablePostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach void resetNames() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("TRUNCATE teaching_names"); s.execute("INSERT INTO teaching_names VALUES ('existing')");
        }
    }

    @Test void failureOnSecondInsertRollsBackFirstAndPreservesEarlierCommit() throws Exception {
        SQLException error = assertThrows(SQLException.class, () -> storeNames(dataSource, List.of("first", "existing")));
        assertEquals("23505", error.getSQLState());
        assertEquals(List.of("existing"), readNames());
    }

    @Test void successfulBatchCommitsBothAndEmptyBatchChangesNothing() throws Exception {
        storeNames(dataSource, List.of("first", "second"));
        assertEquals(List.of("existing", "first", "second"), readNames());
        storeNames(dataSource, List.of());
        assertEquals(List.of("existing", "first", "second"), readNames());
    }

    @Test void laterDuplicateRollsBackEveryRowOfThatBatch() throws Exception {
        storeNames(dataSource, List.of("first", "second"));
        SQLException error = assertThrows(SQLException.class, () -> storeNames(dataSource, List.of("third", "first")));
        assertEquals("23505", error.getSQLState());
        assertEquals(List.of("existing", "first", "second"), readNames());
    }

    @Test void duplicateInsideOneFreshBatchRollsBackEveryInsertInThatCall() throws Exception {
        SQLException error = assertThrows(SQLException.class,
                () -> storeNames(dataSource, List.of("first", "second", "first")));
        assertEquals("23505", error.getSQLState());
        assertEquals(List.of("existing"), readNames());
    }

    static void storeNames(DataSource ds, List<String> names) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement("INSERT INTO teaching_names(name) VALUES (?)")) {
                    for (String name : names) { p.setString(1, name); p.executeUpdate(); }
                }
                c.commit();
            } catch (SQLException | RuntimeException original) {
                try { c.rollback(); }
                catch (SQLException rollbackFailure) { original.addSuppressed(rollbackFailure); }
                throw original;
            }
        }
    }

    private static List<String> readNames() throws Exception {
        try (Connection c=dataSource.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT name FROM teaching_names ORDER BY name")) {
            List<String> out=new ArrayList<>(); while(r.next()) out.add(r.getString(1)); return List.copyOf(out);
        }
    }
}
