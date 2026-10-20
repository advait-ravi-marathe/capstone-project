package assignments.reboot;

import assignments.insurance.SelectionAuditService;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real PostgreSQL transaction cases; learner-owned orchestration remains TODO. */
@Tag("learnerAcceptance")
class Day26ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private SelectionAuditService service;
    private static final Instant FIXED_TIME = Instant.parse("2026-10-20T12:00:00Z");

    @BeforeAll
    static void startPostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE selections (resource_id TEXT PRIMARY KEY)");
            s.execute("CREATE TABLE selection_audit (action TEXT NOT NULL, resource_id TEXT NOT NULL, actor_id TEXT NOT NULL, occurred_at TIMESTAMPTZ NOT NULL)");
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach
    void reset() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP TRIGGER IF EXISTS fail_audit_insert ON selection_audit");
            s.execute("DROP FUNCTION IF EXISTS fail_audit_insert_fn()");
            s.execute("TRUNCATE selection_audit, selections");
        }
        service = new SelectionAuditService(dataSource);
    }

    @Test
    void firstSelectionAndMinimalAuditCommitTogether() throws Exception {
        assertTrue(service.recordFirstSelection("selection-17", "actor-4", FIXED_TIME));
        assertEquals(1, count("selections"));
        assertEquals(1, count("selection_audit"));
        assertEquals(List.of("SELECTION_CREATED|selection-17|actor-4|2026-10-20 12:00:00+00"), auditRows());
        assertFalse(hasPayloadColumn());
    }

    @Test
    void repeatedSelectionDoesNotAppendCreationEvent() throws Exception {
        execute("INSERT INTO selections VALUES ('selection-17')");
        execute("INSERT INTO selection_audit VALUES ('SELECTION_CREATED', 'selection-17', 'actor-4', TIMESTAMPTZ '2026-10-20 12:00:00+00')");
        assertFalse(service.recordFirstSelection("selection-17", "actor-4", FIXED_TIME));
        assertEquals(1, count("selection_audit"));
    }

    @Test
    void auditFailureRollsBackSelectionInsert() throws Exception {
        execute("CREATE FUNCTION fail_audit_insert_fn() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'fixture failure'; END $$");
        execute("CREATE TRIGGER fail_audit_insert BEFORE INSERT ON selection_audit FOR EACH ROW EXECUTE FUNCTION fail_audit_insert_fn()");
        assertThrows(SQLException.class, () -> service.recordFirstSelection("selection-17", "actor-4", FIXED_TIME));
        assertEquals(0, count("selections"));
        assertEquals(0, count("selection_audit"));
    }

    private static int count(String table) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT count(*) FROM " + table)) { r.next(); return r.getInt(1); }
    }
    private static List<String> auditRows() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT action, resource_id, actor_id, occurred_at FROM selection_audit")) {
            List<String> rows = new ArrayList<>(); while (r.next()) rows.add(r.getString(1)+"|"+r.getString(2)+"|"+r.getString(3)+"|"+r.getString(4)); return List.copyOf(rows);
        }
    }
    private static boolean hasPayloadColumn() throws Exception {
        try (Connection c = dataSource.getConnection(); ResultSet columns = c.getMetaData().getColumns(null, null, "selection_audit", null)) {
            while (columns.next()) if (columns.getString("COLUMN_NAME").toLowerCase().contains("payload")) return true;
            return false;
        }
    }
    private static void execute(String sql) throws Exception { try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute(sql); } }
}
