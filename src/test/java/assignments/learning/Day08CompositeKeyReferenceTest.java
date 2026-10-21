package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Day08CompositeKeyReferenceTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll static void startDisposablePostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE teaching_revisions (team_id TEXT NOT NULL, revision_id TEXT NOT NULL, PRIMARY KEY(team_id,revision_id))");
            s.execute("CREATE TABLE teaching_publications (team_id TEXT NOT NULL, revision_id TEXT NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(team_id,revision_id), FOREIGN KEY(team_id,revision_id) REFERENCES teaching_revisions(team_id,revision_id))");
        }
    }

    @AfterAll static void closeDisposablePostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach void clearRows() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("TRUNCATE teaching_publications, teaching_revisions");
        }
    }

    @Test void exactParentPairIsRequired() throws Exception {
        execute("INSERT INTO teaching_revisions VALUES ('red','r1'), ('blue','r2')");
        execute("INSERT INTO teaching_publications VALUES ('red','r1',0)");
        assertState("23503", "INSERT INTO teaching_publications VALUES ('blue','r1',0)");
        assertState("23503", "INSERT INTO teaching_publications VALUES ('red','missing',1)");
    }

    @Test void duplicatePairIsRejectedEvenWithAnotherPosition() throws Exception {
        execute("INSERT INTO teaching_revisions VALUES ('red','r1')");
        execute("INSERT INTO teaching_publications VALUES ('red','r1',0)");
        assertState("23505", "INSERT INTO teaching_publications VALUES ('red','r1',2)");
    }

    @Test void requiredColumnsRejectNullAndDistinctVersionsCanShareTeam() throws Exception {
        assertState("23502", "INSERT INTO teaching_revisions VALUES ('red',NULL)");
        execute("INSERT INTO teaching_revisions VALUES ('red','r1'), ('red','r2')");
        assertState("23502", "INSERT INTO teaching_publications VALUES ('red','r1',NULL)");
        execute("INSERT INTO teaching_publications VALUES ('red','r1',0), ('red','r2',1)");
    }

    @Test void variationCanReuseRevisionNameForDifferentTeamOnlyWithMatchingPair() throws Exception {
        execute("INSERT INTO teaching_revisions VALUES ('green','r1')");
        execute("INSERT INTO teaching_publications VALUES ('green','r1',0)");
        assertState("23503", "INSERT INTO teaching_publications VALUES ('green','r2',1)");
    }

    private static void execute(String sql) throws SQLException {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute(sql); }
    }

    private static void assertState(String state, String sql) {
        SQLException error = assertThrows(SQLException.class, () -> execute(sql));
        assertEquals(state, error.getSQLState(), sql);
    }
}
